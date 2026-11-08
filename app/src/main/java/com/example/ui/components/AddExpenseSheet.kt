package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod
import com.example.domain.BudgetEngine
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    sheetState: SheetState,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSaveExpense: (amount: BigDecimal, category: String, description: String, paymentMethod: String) -> Unit
) {
    var amountString by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.UPI) }
    var description by remember { mutableStateOf("") }

    val currencySymbol = when (currencyCode) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> "₹"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Giant Amount Display
            val displayAmount = if (amountString.isBlank()) "0" else amountString
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currencySymbol,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = displayAmount,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick increment chips (+50, +100, +500, +1000)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(50, 100, 500, 1000).forEach { inc ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .clickable {
                                val current = amountString.toBigDecimalOrNull() ?: BigDecimal.ZERO
                                amountString = current.add(BigDecimal(inc)).toPlainString()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+$inc",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection (Horizontal Scroll with Icons)
            Text(
                text = "Select Category",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpenseCategory.entries.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    val catColor = Color(cat.defaultColor)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) catColor.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) catColor else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = ExpenseCategory.getIcon(cat),
                                contentDescription = cat.displayName,
                                tint = if (isSelected) catColor else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method Chips
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentMethod.entries.forEach { method ->
                    FilterChip(
                        selected = method == selectedPaymentMethod,
                        onClick = { selectedPaymentMethod = method },
                        label = { Text(method.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = EmeraldPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description / Note input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Note / Description (Optional)", color = TextMuted) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_note_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tactile Numeric Keypad (4 rows: 1-3, 4-6, 7-9, .-0-⌫)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val keyRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf(".", "0", "BACK")
                )

                keyRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        when (key) {
                                            "BACK" -> {
                                                if (amountString.isNotEmpty()) {
                                                    amountString = amountString.dropLast(1)
                                                }
                                            }
                                            "." -> {
                                                if (!amountString.contains(".")) {
                                                    amountString = if (amountString.isEmpty()) "0." else "$amountString."
                                                }
                                            }
                                            else -> {
                                                if (amountString == "0") {
                                                    amountString = key
                                                } else {
                                                    val parts = amountString.split(".")
                                                    if (parts.size == 1 || parts[1].length < 2) {
                                                        amountString += key
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (key == "BACK") {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Backspace",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Big Save Expense Button
            val validAmount = amountString.toBigDecimalOrNull()
            val canSave = validAmount != null && validAmount > BigDecimal.ZERO

            Button(
                onClick = {
                    if (canSave && validAmount != null) {
                        onSaveExpense(
                            validAmount,
                            selectedCategory.displayName,
                            description.trim(),
                            selectedPaymentMethod.label
                        )
                    }
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_expense_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (canSave) "Save ${BudgetEngine.formatCurrency(validAmount!!, currencyCode)}" else "Enter Amount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
