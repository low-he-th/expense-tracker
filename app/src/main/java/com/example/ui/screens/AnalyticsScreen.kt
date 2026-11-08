package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategorySpending
import com.example.data.model.ExpenseCategory
import com.example.data.model.MonthlyAnalytics
import com.example.domain.BudgetEngine
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonOverspent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.math.BigDecimal
import java.time.YearMonth

@Composable
fun AnalyticsScreen(
    currentMonth: YearMonth,
    onMonthChanged: (YearMonth) -> Unit,
    analytics: MonthlyAnalytics?,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Screen Header
        item {
            Text(
                text = "Financial Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }

        // Month Selector Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onMonthChanged(currentMonth.minusMonths(1)) },
                        modifier = Modifier.testTag("prev_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            tint = TextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = analytics?.monthTitle ?: currentMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = { onMonthChanged(currentMonth.plusMonths(1)) },
                        modifier = Modifier.testTag("next_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            tint = TextPrimary
                        )
                    }
                }
            }
        }

        // Monthly Spent vs Available Budget Card
        if (analytics != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Monthly Spending vs Budget",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val budgetUtilPct = if (analytics.totalAvailableBudget > BigDecimal.ZERO) {
                            (analytics.totalSpent.divide(analytics.totalAvailableBudget, 4, java.math.RoundingMode.HALF_UP).toFloat()).coerceIn(0f, 1.5f)
                        } else 0f

                        val isExceeded = analytics.totalSpent > analytics.totalAvailableBudget

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = BudgetEngine.formatCurrency(analytics.totalSpent, currencyCode),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isExceeded) CrimsonOverspent else TextPrimary
                            )
                            Text(
                                text = "of ${BudgetEngine.formatCurrency(analytics.totalAvailableBudget, currencyCode)}",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { budgetUtilPct.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (isExceeded) CrimsonOverspent else EmeraldPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${(budgetUtilPct * 100).toInt()}% of monthly budget utilized",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isExceeded) CrimsonOverspent else TextSecondary
                        )
                    }
                }
            }

            // Quick Stats Grid: Daily Avg, Discipline, Top Category
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stat 1: Daily Average
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Daily Average", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = BudgetEngine.formatCurrency(analytics.averageDailySpent, currencyCode),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    // Stat 2: Days on Budget
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Discipline", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${analytics.daysWithinBudget}d ok",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (analytics.daysOverspent > 0) {
                                    Text(
                                        text = "${analytics.daysOverspent}d over",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CrimsonOverspent
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Category Breakdown Section
            item {
                Text(
                    text = "Category Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (analytics.categoryBreakdown.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expenses recorded this month",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(analytics.categoryBreakdown) { cat ->
                    CategoryProgressCard(cat = cat, currencyCode = currencyCode)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CategoryProgressCard(cat: CategorySpending, currencyCode: String) {
    val categoryIcon = ExpenseCategory.getIcon(cat.category)
    val catColor = Color(cat.category.defaultColor)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(catColor.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = catColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cat.category.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${cat.transactionCount} transactions • ${(cat.percentageOfTotal * 100).toInt()}% of total",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Text(
                    text = BudgetEngine.formatCurrency(cat.totalAmount, currencyCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { cat.percentageOfTotal.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = catColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
