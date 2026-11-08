package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class BudgetEngineTest {

    @Test
    fun testCase1_800Allowance_200Expense_leaves600Remaining() {
        val allowance = BigDecimal("800.00")
        val spent = BigDecimal("200.00")

        val remaining = BudgetEngine.calculateRemainingAllowance(allowance, spent)
        val overspent = BudgetEngine.calculateOverspent(allowance, spent)

        assertEquals(BigDecimal("600.00"), remaining)
        assertEquals(BigDecimal.ZERO, overspent)
    }

    @Test
    fun testCase2_800Allowance_1000Expense_resultsIn200Overspent() {
        val allowance = BigDecimal("800.00")
        val spent = BigDecimal("1000.00")

        val remaining = BudgetEngine.calculateRemainingAllowance(allowance, spent)
        val overspent = BudgetEngine.calculateOverspent(allowance, spent)

        assertEquals(BigDecimal.ZERO, remaining)
        assertEquals(BigDecimal("200.00"), overspent)
    }

    @Test
    fun testCase4_noExpenses_leavesFullAllowance() {
        val allowance = BigDecimal("800.00")
        val spent = BigDecimal.ZERO

        val remaining = BudgetEngine.calculateRemainingAllowance(allowance, spent)
        val overspent = BudgetEngine.calculateOverspent(allowance, spent)

        assertEquals(allowance, remaining)
        assertEquals(BigDecimal.ZERO, overspent)
    }

    @Test
    fun testMonthlySpendingBudget_deductsFixedAndSavings() {
        // Take home: 42,000, Fixed: 10,000, Savings: 8,000 -> Available: 24,000
        val takeHome = BigDecimal("42000.00")
        val fixed = BigDecimal("10000.00")
        val savings = BigDecimal("8000.00")

        val available = BudgetEngine.calculateMonthlySpendingBudget(takeHome, fixed, savings)
        assertEquals(BigDecimal("24000.00"), available)

        // In a 30-day month -> 24000 / 30 = 800.00
        val daily = BudgetEngine.calculateDailyAllowance(available, 30)
        assertEquals(BigDecimal("800.00"), daily)
    }

    @Test
    fun testFebruaryDays_leapVsNonLeap() {
        // 2024 is a leap year (29 days)
        val leapDays = BudgetEngine.getDaysInMonth(2024, 2)
        assertEquals(29, leapDays)

        // 2025 is not a leap year (28 days)
        val regularDays = BudgetEngine.getDaysInMonth(2025, 2)
        assertEquals(28, regularDays)
    }

    @Test
    fun testCurrencyFormatting() {
        val inr = BudgetEngine.formatCurrency(BigDecimal("800.00"), "INR")
        assertEquals("₹800.00", inr)

        val usd = BudgetEngine.formatCurrency(BigDecimal("1250.50"), "USD")
        assertEquals("$1,250.50", usd)
    }
}
