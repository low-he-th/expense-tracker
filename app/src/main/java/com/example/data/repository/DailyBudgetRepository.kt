package com.example.data.repository

import com.example.data.local.dao.SalaryProfileDao
import com.example.data.local.dao.SavingsGoalDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.SalaryProfileEntity
import com.example.data.local.entity.SavingsContributionEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.CategorySpending
import com.example.data.model.DailyBudgetSummary
import com.example.data.model.DailyTrendItem
import com.example.data.model.ExpenseCategory
import com.example.data.model.MonthlyAnalytics
import com.example.domain.BudgetEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class DailyBudgetRepository(
    private val salaryProfileDao: SalaryProfileDao,
    private val transactionDao: TransactionDao,
    private val savingsGoalDao: SavingsGoalDao
) {

    private val defaultProfile = SalaryProfileEntity(
        id = 1L,
        annualCtc = BigDecimal("600000.00"),
        monthlyTakeHome = BigDecimal("42000.00"),
        monthlyFixedExpenses = BigDecimal("10000.00"),
        monthlySavingsTarget = BigDecimal("8000.00"),
        salaryCreditDay = 1,
        currency = "INR",
        effectiveDate = LocalDate.now().withDayOfMonth(1).toString()
    )

    fun observeSalaryProfile(): Flow<SalaryProfileEntity> {
        return salaryProfileDao.getSalaryProfileFlow().map { it ?: defaultProfile }
    }

    suspend fun getSalaryProfile(): SalaryProfileEntity {
        return salaryProfileDao.getSalaryProfile() ?: defaultProfile
    }

    suspend fun updateSalaryProfile(profile: SalaryProfileEntity) {
        salaryProfileDao.insertOrUpdateProfile(profile)
    }

    fun observeTodayBudgetSummary(): Flow<DailyBudgetSummary> {
        return observeDateBudgetSummary(LocalDate.now())
    }

    fun observeDateBudgetSummary(date: LocalDate): Flow<DailyBudgetSummary> {
        val dateStr = date.toString()
        val daysInMonth = BudgetEngine.getDaysInMonth(date.year, date.monthValue)

        return combine(
            observeSalaryProfile(),
            transactionDao.getTransactionsByDateFlow(dateStr)
        ) { profile, transactions ->
            val monthlyBudget = BudgetEngine.calculateMonthlySpendingBudget(
                profile.monthlyTakeHome,
                profile.monthlyFixedExpenses,
                profile.monthlySavingsTarget
            )
            val dailyAllowance = BudgetEngine.calculateDailyAllowance(monthlyBudget, daysInMonth)

            val spentToday = transactions.fold(BigDecimal.ZERO) { acc, tx ->
                acc.add(tx.amount)
            }

            val remaining = BudgetEngine.calculateRemainingAllowance(dailyAllowance, spentToday)
            val overspent = BudgetEngine.calculateOverspent(dailyAllowance, spentToday)
            val utilization = BudgetEngine.calculateUtilizationPercentage(dailyAllowance, spentToday)

            DailyBudgetSummary(
                date = date,
                dailyAllowance = dailyAllowance,
                spentToday = spentToday,
                remainingAllowance = remaining,
                overspent = overspent,
                utilizationPercentage = utilization,
                availableMonthlyBudget = monthlyBudget,
                daysInMonth = daysInMonth,
                profile = profile,
                todayTransactions = transactions
            )
        }
    }

    fun observeAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactionsFlow()
    }

    suspend fun recordExpense(
        amount: BigDecimal,
        category: String,
        description: String,
        paymentMethod: String,
        date: LocalDate = LocalDate.now()
    ): Long {
        val transaction = TransactionEntity(
            amount = amount,
            category = category,
            description = description,
            paymentMethod = paymentMethod,
            transactionDate = date.toString(),
            timestamp = System.currentTimeMillis()
        )
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    fun observeSavingsGoals(): Flow<List<SavingsGoalEntity>> {
        return savingsGoalDao.getAllGoalsFlow()
    }

    suspend fun addSavingsGoal(
        name: String,
        targetAmount: BigDecimal,
        targetDate: String,
        description: String
    ): Long {
        val goal = SavingsGoalEntity(
            name = name,
            targetAmount = targetAmount,
            currentAmount = BigDecimal.ZERO,
            targetDate = targetDate,
            description = description,
            status = "IN_PROGRESS"
        )
        return savingsGoalDao.insertGoal(goal)
    }

    suspend fun addSavingsContribution(
        goalId: Long,
        amount: BigDecimal,
        notes: String
    ) {
        val goal = savingsGoalDao.getGoalById(goalId) ?: return
        val updatedAmount = goal.currentAmount.add(amount)
        val status = if (updatedAmount >= goal.targetAmount) "COMPLETED" else goal.status

        savingsGoalDao.updateGoal(
            goal.copy(
                currentAmount = updatedAmount,
                status = status
            )
        )
        savingsGoalDao.insertContribution(
            SavingsContributionEntity(
                goalId = goalId,
                amount = amount,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.deleteGoal(goal)
    }

    fun observeMonthlyAnalytics(yearMonth: YearMonth): Flow<MonthlyAnalytics> {
        val startDate = yearMonth.atDay(1).toString()
        val endDate = yearMonth.atEndOfMonth().toString()
        val daysInMonth = yearMonth.lengthOfMonth()

        return combine(
            observeSalaryProfile(),
            transactionDao.getTransactionsBetweenDatesFlow(startDate, endDate)
        ) { profile, transactions ->
            val monthlyBudget = BudgetEngine.calculateMonthlySpendingBudget(
                profile.monthlyTakeHome,
                profile.monthlyFixedExpenses,
                profile.monthlySavingsTarget
            )
            val dailyAllowance = BudgetEngine.calculateDailyAllowance(monthlyBudget, daysInMonth)

            val totalSpent = transactions.fold(BigDecimal.ZERO) { acc, tx ->
                acc.add(tx.amount)
            }

            // Group by category
            val categoryMap = transactions.groupBy { it.category }
            val categoryList = categoryMap.map { (catName, txList) ->
                val catAmount = txList.fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }
                val pct = if (totalSpent > BigDecimal.ZERO) {
                    catAmount.divide(totalSpent, 4, RoundingMode.HALF_UP).toFloat()
                } else 0f
                CategorySpending(
                    category = ExpenseCategory.fromString(catName),
                    totalAmount = catAmount,
                    transactionCount = txList.size,
                    percentageOfTotal = pct
                )
            }.sortedByDescending { it.totalAmount }

            // Group by date for daily trends
            val txByDate = transactions.groupBy { it.transactionDate }
            var onBudgetDays = 0
            var overspentDays = 0

            val trendList = (1..daysInMonth).map { day ->
                val date = yearMonth.atDay(day)
                val dateStr = date.toString()
                val dayTxs = txByDate[dateStr] ?: emptyList()
                val daySpent = dayTxs.fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }
                val isOver = daySpent > dailyAllowance
                if (daySpent > BigDecimal.ZERO) {
                    if (isOver) overspentDays++ else onBudgetDays++
                }
                DailyTrendItem(
                    date = date,
                    dayOfMonth = day,
                    totalSpent = daySpent,
                    dailyAllowance = dailyAllowance,
                    isOverspent = isOver
                )
            }

            val passedDays = if (yearMonth == YearMonth.now()) LocalDate.now().dayOfMonth else daysInMonth
            val avgDaily = if (passedDays > 0) {
                totalSpent.divide(BigDecimal(passedDays), 2, RoundingMode.HALF_UP)
            } else BigDecimal.ZERO

            val monthTitle = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US))

            MonthlyAnalytics(
                monthTitle = monthTitle,
                totalSpent = totalSpent,
                totalAvailableBudget = monthlyBudget,
                averageDailySpent = avgDaily,
                highestCategory = categoryList.firstOrNull()?.category,
                daysWithinBudget = onBudgetDays,
                daysOverspent = overspentDays,
                categoryBreakdown = categoryList,
                dailyTrends = trendList
            )
        }
    }
}
