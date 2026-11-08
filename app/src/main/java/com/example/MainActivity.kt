package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.DailyBudgetRepository
import com.example.ui.DailyWorthApp
import com.example.ui.DailyWorthViewModel
import com.example.ui.theme.DailyWorthTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = DailyBudgetRepository(
            salaryProfileDao = database.salaryProfileDao(),
            transactionDao = database.transactionDao(),
            savingsGoalDao = database.savingsGoalDao()
        )

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DailyWorthViewModel(repository) as T
            }
        }

        val viewModel = ViewModelProvider(this, viewModelFactory)[DailyWorthViewModel::class.java]

        setContent {
            DailyWorthTheme {
                DailyWorthApp(viewModel = viewModel)
            }
        }
    }
}
