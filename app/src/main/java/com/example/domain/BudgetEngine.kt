package com.example.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

/**
 * Pure domain logic for DailyWorth financial calculations.
 * Implements strict BigDecimal math with zero-rollover daily allowance principles.
 */
object BudgetEngine {

    /**
     * Available Monthly Budget = Monthly Take-home Salary - Fixed Monthly Expenses - Monthly Savings Allocation
     */
    fun calculateMonthlySpendingBudget(
        monthlyTakeHome: BigDecimal,
        fixedExpenses: BigDecimal,
        monthlySavingsTarget: BigDecimal
    ): BigDecimal {
        val net = monthlyTakeHome.subtract(fixedExpenses).subtract(monthlySavingsTarget)
        return net.max(BigDecimal.ZERO)
    }

    /**
     * Daily Allowance = Available Monthly Budget / Number of Days in Budget Period
     */
    fun calculateDailyAllowance(
        availableMonthlyBudget: BigDecimal,
        daysInPeriod: Int
    ): BigDecimal {
        if (daysInPeriod <= 0) return BigDecimal.ZERO
        return availableMonthlyBudget.divide(
            BigDecimal(daysInPeriod),
            2,
            RoundingMode.HALF_UP
        )
    }

    /**
     * Remaining Allowance = max(0, Daily Allowance - Today's Eligible Expenses)
     */
    fun calculateRemainingAllowance(
        dailyAllowance: BigDecimal,
        spentToday: BigDecimal
    ): BigDecimal {
        val remaining = dailyAllowance.subtract(spentToday)
        return remaining.max(BigDecimal.ZERO)
    }

    /**
     * Overspent = max(0, Today's Eligible Expenses - Daily Allowance)
     */
    fun calculateOverspent(
        dailyAllowance: BigDecimal,
        spentToday: BigDecimal
    ): BigDecimal {
        val over = spentToday.subtract(dailyAllowance)
        return over.max(BigDecimal.ZERO)
    }

    /**
     * Budget utilization percentage (0% to 100%+).
     */
    fun calculateUtilizationPercentage(
        dailyAllowance: BigDecimal,
        spentToday: BigDecimal
    ): Float {
        if (dailyAllowance.compareTo(BigDecimal.ZERO) <= 0) {
            return if (spentToday > BigDecimal.ZERO) 1.0f else 0.0f
        }
        val ratio = spentToday.divide(dailyAllowance, 4, RoundingMode.HALF_UP).toFloat()
        return ratio
    }

    /**
     * Helper to get total days in a given calendar month.
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        val yearMonth = YearMonth.of(year, month)
        return yearMonth.lengthOfMonth()
    }

    /**
     * Format currency with symbol.
     */
    fun formatCurrency(amount: BigDecimal, currencyCode: String = "INR"): String {
        val symbol = when (currencyCode.uppercase(Locale.ROOT)) {
            "INR" -> "₹"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            else -> "₹"
        }
        val rounded = amount.setScale(2, RoundingMode.HALF_UP)
        // Format with comma groups
        val plain = String.format(Locale.US, "%,.2f", rounded.toDouble())
        return "$symbol$plain"
    }
}
