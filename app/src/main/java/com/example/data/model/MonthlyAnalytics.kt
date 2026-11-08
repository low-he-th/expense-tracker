package com.example.data.model

import java.math.BigDecimal
import java.time.LocalDate

data class CategorySpending(
    val category: ExpenseCategory,
    val totalAmount: BigDecimal,
    val transactionCount: Int,
    val percentageOfTotal: Float
)

data class DailyTrendItem(
    val date: LocalDate,
    val dayOfMonth: Int,
    val totalSpent: BigDecimal,
    val dailyAllowance: BigDecimal,
    val isOverspent: Boolean
)

data class MonthlyAnalytics(
    val monthTitle: String,
    val totalSpent: BigDecimal,
    val totalAvailableBudget: BigDecimal,
    val averageDailySpent: BigDecimal,
    val highestCategory: ExpenseCategory?,
    val daysWithinBudget: Int,
    val daysOverspent: Int,
    val categoryBreakdown: List<CategorySpending>,
    val dailyTrends: List<DailyTrendItem>
)
