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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.local.entity.SavingsGoalEntity
import com.example.domain.BudgetEngine
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.math.BigDecimal
import java.time.LocalDate

@Composable
fun SavingsGoalsScreen(
    goals: List<SavingsGoalEntity>,
    currencyCode: String,
    onAddGoal: (name: String, targetAmount: BigDecimal, targetDate: String, description: String) -> Unit,
    onAddContribution: (goalId: Long, amount: BigDecimal, notes: String) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewGoalDialog by remember { mutableStateOf(false) }
    var activeGoalForContribution by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("savings_goals_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Savings Goals",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Allocated from monthly savings, never daily allowance",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Add New Goal CTA Button
        item {
            Button(
                onClick = { showNewGoalDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("add_new_goal_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create New Savings Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Goals List or Empty State
        if (goals.isEmpty()) {
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
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No savings targets yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set up an Emergency Fund, Travel goal, or Gadget purchase to stay motivated.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                GoalItemCard(
                    goal = goal,
                    currencyCode = currencyCode,
                    onContributeClick = { activeGoalForContribution = goal },
                    onDeleteClick = { onDeleteGoal(goal) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // New Goal Dialog
    if (showNewGoalDialog) {
        CreateGoalDialog(
            currencyCode = currencyCode,
            onDismiss = { showNewGoalDialog = false },
            onConfirm = { name, target, date, desc ->
                onAddGoal(name, target, date, desc)
                showNewGoalDialog = false
            }
        )
    }

    // Add Contribution Dialog
    activeGoalForContribution?.let { goal ->
        ContributeDialog(
            goal = goal,
            currencyCode = currencyCode,
            onDismiss = { activeGoalForContribution = null },
            onConfirm = { amount, note ->
                onAddContribution(goal.id, amount, note)
                activeGoalForContribution = null
            }
        )
    }
}

@Composable
private fun GoalItemCard(
    goal: SavingsGoalEntity,
    currencyCode: String,
    onContributeClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val progress = if (goal.targetAmount > BigDecimal.ZERO) {
        (goal.currentAmount.divide(goal.targetAmount, 4, java.math.RoundingMode.HALF_UP).toFloat()).coerceIn(0f, 1f)
    } else 0f
    val isCompleted = goal.currentAmount >= goal.targetAmount

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) EmeraldPrimary.copy(alpha = 0.15f)
                                else AccentBlue.copy(alpha = 0.15f)
                            )
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Savings,
                            contentDescription = null,
                            tint = if (isCompleted) EmeraldPrimary else AccentBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (goal.description.isNotBlank()) {
                            Text(
                                text = goal.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Goal",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isCompleted) EmeraldPrimary else AccentBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${(progress * 100).toInt()}% achieved",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) EmeraldPrimary else AccentBlue
                    )
                    Text(
                        text = "${BudgetEngine.formatCurrency(goal.currentAmount, currencyCode)} of ${BudgetEngine.formatCurrency(goal.targetAmount, currencyCode)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (!isCompleted) {
                    Button(
                        onClick = onContributeClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("+ Allocate", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Goal Completed 🎉", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateGoalDialog(
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, target: BigDecimal, date: String, desc: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Savings Goal", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name (e.g. Laptop, Bike)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue)
                )
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("Target Amount ($currencyCode)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue)
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description / Note (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue)
                )
            }
        },
        confirmButton = {
            val amount = targetStr.toBigDecimalOrNull()
            Button(
                onClick = {
                    if (name.isNotBlank() && amount != null && amount > BigDecimal.ZERO) {
                        onConfirm(name.trim(), amount, LocalDate.now().plusMonths(3).toString(), desc.trim())
                    }
                },
                enabled = name.isNotBlank() && amount != null && amount > BigDecimal.ZERO,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Create Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ContributeDialog(
    goal: SavingsGoalEntity,
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: BigDecimal, note: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Allocate to ${goal.name}", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Savings allocations are funded from your monthly savings budget and are NOT deducted from today's daily allowance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Contribution Amount ($currencyCode)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notes (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue)
                )
            }
        },
        confirmButton = {
            val amount = amountStr.toBigDecimalOrNull()
            Button(
                onClick = {
                    if (amount != null && amount > BigDecimal.ZERO) {
                        onConfirm(amount, note.trim())
                    }
                },
                enabled = amount != null && amount > BigDecimal.ZERO,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Add Allocation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
