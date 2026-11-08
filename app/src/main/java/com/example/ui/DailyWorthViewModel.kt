package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.SalaryProfileEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.DailyBudgetSummary
import com.example.data.model.MonthlyAnalytics
import com.example.data.repository.DailyBudgetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class DailyWorthViewModel(
    private val repository: DailyBudgetRepository
) : ViewModel() {

    val todaySummary: StateFlow<DailyBudgetSummary?> = repository.observeTodayBudgetSummary()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val salaryProfile: StateFlow<SalaryProfileEntity> = repository.observeSalaryProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SalaryProfileEntity()
        )

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.observeAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.observeSavingsGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val selectedDateSummary: StateFlow<DailyBudgetSummary?> = _selectedDate
        .flatMapLatest { date -> repository.observeDateBudgetSummary(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _currentAnalyticsMonth = MutableStateFlow(YearMonth.now())
    val currentAnalyticsMonth: StateFlow<YearMonth> = _currentAnalyticsMonth.asStateFlow()

    val monthlyAnalytics: StateFlow<MonthlyAnalytics?> = _currentAnalyticsMonth
        .flatMapLatest { ym -> repository.observeMonthlyAnalytics(ym) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun selectAnalyticsMonth(yearMonth: YearMonth) {
        _currentAnalyticsMonth.value = yearMonth
    }

    fun recordExpense(
        amount: BigDecimal,
        category: String,
        description: String,
        paymentMethod: String,
        date: LocalDate = _selectedDate.value
    ) {
        viewModelScope.launch {
            repository.recordExpense(
                amount = amount,
                category = category,
                description = description,
                paymentMethod = paymentMethod,
                date = date
            )
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun updateSalaryProfile(profile: SalaryProfileEntity) {
        viewModelScope.launch {
            repository.updateSalaryProfile(profile)
        }
    }

    fun addSavingsGoal(name: String, targetAmount: BigDecimal, targetDate: String, description: String) {
        viewModelScope.launch {
            repository.addSavingsGoal(name, targetAmount, targetDate, description)
        }
    }

    fun addSavingsContribution(goalId: Long, amount: BigDecimal, notes: String) {
        viewModelScope.launch {
            repository.addSavingsContribution(goalId, amount, notes)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }
}
