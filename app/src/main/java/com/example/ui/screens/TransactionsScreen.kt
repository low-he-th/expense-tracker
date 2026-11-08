package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.DailyBudgetSummary
import com.example.data.model.ExpenseCategory
import com.example.domain.BudgetEngine
import com.example.ui.components.TransactionRow
import com.example.ui.theme.CrimsonOverspent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TransactionsScreen(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    dateSummary: DailyBudgetSummary?,
    allTransactions: List<TransactionEntity>,
    currencyCode: String,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf<String?>("All") }
    val isToday = selectedDate == LocalDate.now()

    val filteredTransactions = remember(selectedDate, selectedCategoryFilter, allTransactions) {
        val onDate = allTransactions.filter { it.transactionDate == selectedDate.toString() }
        if (selectedCategoryFilter == "All" || selectedCategoryFilter == null) {
            onDate
        } else {
            onDate.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("transactions_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Title
        item {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }

        // Date Picker Bar (Previous Day, Current Date Display, Next Day)
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
                        onClick = { onDateSelected(selectedDate.minusDays(1)) },
                        modifier = Modifier.testTag("prev_date_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Day",
                            tint = TextPrimary
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            if (!isToday) onDateSelected(LocalDate.now())
                        }
                    ) {
                        Text(
                            text = if (isToday) "Today" else selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.US)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (!isToday) {
                            Text(
                                text = "Tap to jump to Today",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary
                            )
                        } else {
                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = { onDateSelected(selectedDate.plusDays(1)) },
                        modifier = Modifier.testTag("next_date_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Day",
                            tint = TextPrimary
                        )
                    }
                }
            }
        }

        // Selected Date's Daily Allowance, Spending & Remaining Summary Card
        item {
            if (dateSummary != null) {
                val isOver = dateSummary.overspent > BigDecimal.ZERO
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Budget Snapshot",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isOver) CrimsonOverspent.copy(alpha = 0.2f)
                                        else EmeraldPrimary.copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isOver) "Overspent" else "Within Limit",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOver) CrimsonOverspent else EmeraldPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Column 1: Allowance
                            Column {
                                Text("Allowance", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(
                                    BudgetEngine.formatCurrency(dateSummary.dailyAllowance, currencyCode),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            // Column 2: Spent
                            Column {
                                Text("Total Spent", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(
                                    BudgetEngine.formatCurrency(dateSummary.spentToday, currencyCode),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOver) CrimsonOverspent else TextPrimary
                                )
                            }

                            // Column 3: Remaining / Over
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    if (isOver) "Overspent By" else "Remaining",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOver) CrimsonOverspent else TextSecondary
                                )
                                Text(
                                    if (isOver) BudgetEngine.formatCurrency(dateSummary.overspent, currencyCode)
                                    else BudgetEngine.formatCurrency(dateSummary.remainingAllowance, currencyCode),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOver) CrimsonOverspent else EmeraldPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val categories = listOf("All") + ExpenseCategory.entries.map { it.displayName }
                categories.forEach { catName ->
                    val isSelected = catName == selectedCategoryFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = catName },
                        label = { Text(catName) },
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

        // Filtered Transactions List
        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No records on this date",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No expenses recorded for ${selectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                TransactionRow(
                    transaction = tx,
                    currencyCode = currencyCode,
                    onDelete = onDeleteTransaction
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
