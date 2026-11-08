package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SalaryProfileEntity
import com.example.domain.BudgetEngine
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.math.BigDecimal
import java.time.LocalDate

@Composable
fun SalaryConfigScreen(
    currentProfile: SalaryProfileEntity,
    onSaveProfile: (SalaryProfileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var takeHomeStr by remember(currentProfile) { mutableStateOf(currentProfile.monthlyTakeHome.toPlainString()) }
    var fixedExpensesStr by remember(currentProfile) { mutableStateOf(currentProfile.monthlyFixedExpenses.toPlainString()) }
    var savingsTargetStr by remember(currentProfile) { mutableStateOf(currentProfile.monthlySavingsTarget.toPlainString()) }
    var annualCtcStr by remember(currentProfile) { mutableStateOf(currentProfile.annualCtc.toPlainString()) }
    var selectedCurrency by remember(currentProfile) { mutableStateOf(currentProfile.currency) }
    var creditDayStr by remember(currentProfile) { mutableStateOf(currentProfile.salaryCreditDay.toString()) }

    var saveSuccessMessage by remember { mutableStateOf(false) }

    // Live preview calculations
    val takeHome = takeHomeStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val fixedExpenses = fixedExpensesStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val savingsTarget = savingsTargetStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val daysInMonth = BudgetEngine.getDaysInMonth(LocalDate.now().year, LocalDate.now().monthValue)

    val previewMonthlyBudget = BudgetEngine.calculateMonthlySpendingBudget(takeHome, fixedExpenses, savingsTarget)
    val previewDailyAllowance = BudgetEngine.calculateDailyAllowance(previewMonthlyBudget, daysInMonth)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("salary_config_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Salary & Budget Calibration",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "Configure your net income and commitments to calculate your safe daily spending limit.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Live Calculated Allowance Preview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Calculated Daily Allowance",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = BudgetEngine.formatCurrency(previewDailyAllowance, selectedCurrency),
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp, fontWeight = FontWeight.ExtraBold),
                        color = TextPrimary
                    )
                    Text(
                        text = "per day ($daysInMonth days in this month)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Available Monthly Pool", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = BudgetEngine.formatCurrency(previewMonthlyBudget, selectedCurrency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Formula", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = "Take-Home - Fixed - Savings",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            }
        }

        // Currency Selector
        item {
            Text("Preferred Currency", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("INR" to "₹ INR", "USD" to "$ USD", "EUR" to "€ EUR", "GBP" to "£ GBP").forEach { (code, label) ->
                    FilterChip(
                        selected = selectedCurrency == code,
                        onClick = { selectedCurrency = code },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = EmeraldPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        // Financial Inputs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Income & Commitments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

                    OutlinedTextField(
                        value = takeHomeStr,
                        onValueChange = { takeHomeStr = it },
                        label = { Text("Actual Monthly Take-Home Salary ($selectedCurrency)") },
                        supportingText = { Text("Net salary deposited into your bank account") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary)
                    )

                    OutlinedTextField(
                        value = fixedExpensesStr,
                        onValueChange = { fixedExpensesStr = it },
                        label = { Text("Monthly Fixed Expenses ($selectedCurrency)") },
                        supportingText = { Text("Rent, EMIs, utilities, insurance, subscriptions") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary)
                    )

                    OutlinedTextField(
                        value = savingsTargetStr,
                        onValueChange = { savingsTargetStr = it },
                        label = { Text("Monthly Savings Allocation ($selectedCurrency)") },
                        supportingText = { Text("Dedicated capital saved each month for goals") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary)
                    )

                    OutlinedTextField(
                        value = annualCtcStr,
                        onValueChange = { annualCtcStr = it },
                        label = { Text("Annual CTC / Gross Salary ($selectedCurrency)") },
                        supportingText = { Text("For reference and employment records") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary)
                    )

                    OutlinedTextField(
                        value = creditDayStr,
                        onValueChange = { creditDayStr = it },
                        label = { Text("Salary Credit Date (Day 1-31)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary)
                    )
                }
            }
        }

        // Save Button
        item {
            val canSave = takeHome > BigDecimal.ZERO
            Button(
                onClick = {
                    if (canSave) {
                        val updated = currentProfile.copy(
                            annualCtc = annualCtcStr.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                            monthlyTakeHome = takeHome,
                            monthlyFixedExpenses = fixedExpenses,
                            monthlySavingsTarget = savingsTarget,
                            salaryCreditDay = creditDayStr.toIntOrNull()?.coerceIn(1, 31) ?: 1,
                            currency = selectedCurrency
                        )
                        onSaveProfile(updated)
                        saveSuccessMessage = true
                    }
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_salary_config_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Save Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (saveSuccessMessage) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.15f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓ Configuration saved! Daily allowance recalculated.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
