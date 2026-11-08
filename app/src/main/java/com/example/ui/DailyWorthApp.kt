package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddExpenseSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SalaryConfigScreen
import com.example.ui.screens.SavingsGoalsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    DASHBOARD("Home"),
    TRANSACTIONS("History"),
    ANALYTICS("Analytics"),
    GOALS("Goals"),
    SETTINGS("Calibrate")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyWorthApp(viewModel: DailyWorthViewModel) {
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var isAddExpenseOpen by remember { mutableStateOf(false) }

    val todaySummary by viewModel.todaySummary.collectAsStateWithLifecycle()
    val salaryProfile by viewModel.salaryProfile.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedDateSummary by viewModel.selectedDateSummary.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentAnalyticsMonth.collectAsStateWithLifecycle()
    val monthlyAnalytics by viewModel.monthlyAnalytics.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars,
                tonalElevation = 8.dp
            ) {
                // 1. Dashboard
                NavigationBarItem(
                    selected = currentTab == AppTab.DASHBOARD,
                    onClick = { currentTab = AppTab.DASHBOARD },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.DASHBOARD) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                    )
                )

                // 2. Transactions
                NavigationBarItem(
                    selected = currentTab == AppTab.TRANSACTIONS,
                    onClick = { currentTab = AppTab.TRANSACTIONS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.TRANSACTIONS) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                    )
                )

                // 3. Analytics
                NavigationBarItem(
                    selected = currentTab == AppTab.ANALYTICS,
                    onClick = { currentTab = AppTab.ANALYTICS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.ANALYTICS) Icons.Default.BarChart else Icons.Outlined.BarChart,
                            contentDescription = "Analytics"
                        )
                    },
                    label = { Text("Analytics") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                    )
                )

                // 4. Goals
                NavigationBarItem(
                    selected = currentTab == AppTab.GOALS,
                    onClick = { currentTab = AppTab.GOALS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.GOALS) Icons.Default.Savings else Icons.Outlined.Savings,
                            contentDescription = "Goals"
                        )
                    },
                    label = { Text("Goals") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                    )
                )

                // 5. Settings / Calibrate
                NavigationBarItem(
                    selected = currentTab == AppTab.SETTINGS,
                    onClick = { currentTab = AppTab.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.SETTINGS) Icons.Default.Tune else Icons.Outlined.Tune,
                            contentDescription = "Calibrate"
                        )
                    },
                    label = { Text("Calibrate") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                    )
                )
            }
        },
        floatingActionButton = {
            if (currentTab == AppTab.DASHBOARD || currentTab == AppTab.TRANSACTIONS) {
                FloatingActionButton(
                    onClick = { isAddExpenseOpen = true },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    modifier = Modifier.testTag("global_add_expense_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Expense",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
                when (tab) {
                    AppTab.DASHBOARD -> DashboardScreen(
                        summary = todaySummary,
                        goals = savingsGoals,
                        onAddExpenseClick = { isAddExpenseOpen = true },
                        onDeleteTransaction = { id -> viewModel.deleteTransaction(id) },
                        onNavigateToTransactions = { currentTab = AppTab.TRANSACTIONS },
                        onNavigateToGoals = { currentTab = AppTab.GOALS },
                        onNavigateToSettings = { currentTab = AppTab.SETTINGS }
                    )

                    AppTab.TRANSACTIONS -> TransactionsScreen(
                        selectedDate = selectedDate,
                        onDateSelected = { date -> viewModel.selectDate(date) },
                        dateSummary = selectedDateSummary,
                        allTransactions = allTransactions,
                        currencyCode = salaryProfile.currency,
                        onDeleteTransaction = { id -> viewModel.deleteTransaction(id) }
                    )

                    AppTab.ANALYTICS -> AnalyticsScreen(
                        currentMonth = currentMonth,
                        onMonthChanged = { ym -> viewModel.selectAnalyticsMonth(ym) },
                        analytics = monthlyAnalytics,
                        currencyCode = salaryProfile.currency
                    )

                    AppTab.GOALS -> SavingsGoalsScreen(
                        goals = savingsGoals,
                        currencyCode = salaryProfile.currency,
                        onAddGoal = { name, target, date, desc ->
                            viewModel.addSavingsGoal(name, target, date, desc)
                        },
                        onAddContribution = { goalId, amount, notes ->
                            viewModel.addSavingsContribution(goalId, amount, notes)
                        },
                        onDeleteGoal = { goal -> viewModel.deleteSavingsGoal(goal) }
                    )

                    AppTab.SETTINGS -> SalaryConfigScreen(
                        currentProfile = salaryProfile,
                        onSaveProfile = { updated -> viewModel.updateSalaryProfile(updated) }
                    )
                }
            }
        }
    }

    // Quick Add Expense Bottom Sheet
    if (isAddExpenseOpen) {
        AddExpenseSheet(
            sheetState = sheetState,
            currencyCode = salaryProfile.currency,
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                    isAddExpenseOpen = false
                }
            },
            onSaveExpense = { amount, category, description, paymentMethod ->
                viewModel.recordExpense(
                    amount = amount,
                    category = category,
                    description = description,
                    paymentMethod = paymentMethod
                )
                scope.launch {
                    sheetState.hide()
                    isAddExpenseOpen = false
                }
            }
        )
    }
}
