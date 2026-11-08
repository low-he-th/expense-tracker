package com.example.data.model

import com.example.data.local.entity.SalaryProfileEntity
import com.example.data.local.entity.TransactionEntity
import java.math.BigDecimal
import java.time.LocalDate

data class DailyBudgetSummary(
    val date: LocalDate,
    val dailyAllowance: BigDecimal,
    val spentToday: BigDecimal,
    val remainingAllowance: BigDecimal,
    val overspent: BigDecimal,
    val utilizationPercentage: Float,
    val availableMonthlyBudget: BigDecimal,
    val daysInMonth: Int,
    val profile: SalaryProfileEntity,
    val todayTransactions: List<TransactionEntity>
) {
    val isOverspent: Boolean get() = overspent > BigDecimal.ZERO
    val isUnderBudget: Boolean get() = !isOverspent
}
