package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.SalaryProfileDao
import com.example.data.local.dao.SavingsGoalDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.SalaryProfileEntity
import com.example.data.local.entity.SavingsContributionEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate

@Database(
    entities = [
        SalaryProfileEntity::class,
        TransactionEntity::class,
        SavingsGoalEntity::class,
        SavingsContributionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun salaryProfileDao(): SalaryProfileDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dailyworth.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial defaults so user has instant interactive experience
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            seedInitialData(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            val salaryDao = database.salaryProfileDao()
            val transactionDao = database.transactionDao()
            val goalDao = database.savingsGoalDao()

            if (salaryDao.getSalaryProfile() == null) {
                // Default calibrated to example: Take-home ₹42,000, Fixed ₹10,000, Savings ₹8,000
                // Remaining monthly: ₹24,000 -> ₹800/day for 30-day month
                salaryDao.insertOrUpdateProfile(
                    SalaryProfileEntity(
                        id = 1L,
                        annualCtc = BigDecimal("600000.00"),
                        monthlyTakeHome = BigDecimal("42000.00"),
                        monthlyFixedExpenses = BigDecimal("10000.00"),
                        monthlySavingsTarget = BigDecimal("8000.00"),
                        salaryCreditDay = 1,
                        currency = "INR",
                        effectiveDate = LocalDate.now().withDayOfMonth(1).toString()
                    )
                )
            }

            if (transactionDao.getTransactionCount() == 0) {
                val todayStr = LocalDate.now().toString()
                val yesterdayStr = LocalDate.now().minusDays(1).toString()

                // Sample starter transactions
                transactionDao.insertTransaction(
                    TransactionEntity(
                        amount = BigDecimal("140.00"),
                        category = "Food",
                        description = "Healthy Breakfast & Coffee",
                        paymentMethod = "UPI",
                        transactionDate = todayStr,
                        timestamp = System.currentTimeMillis() - 7200000L
                    )
                )
                transactionDao.insertTransaction(
                    TransactionEntity(
                        amount = BigDecimal("95.00"),
                        category = "Travel",
                        description = "Metro transit fare",
                        paymentMethod = "UPI",
                        transactionDate = todayStr,
                        timestamp = System.currentTimeMillis() - 3600000L
                    )
                )
                transactionDao.insertTransaction(
                    TransactionEntity(
                        amount = BigDecimal("450.00"),
                        category = "Food",
                        description = "Grocery run & snacks",
                        paymentMethod = "Debit Card",
                        transactionDate = yesterdayStr,
                        timestamp = System.currentTimeMillis() - 86400000L
                    )
                )
            }

            if (goalDao.getGoalCount() == 0) {
                goalDao.insertGoal(
                    SavingsGoalEntity(
                        name = "Emergency Fund",
                        targetAmount = BigDecimal("100000.00"),
                        currentAmount = BigDecimal("24000.00"),
                        targetDate = LocalDate.now().plusMonths(6).toString(),
                        description = "3 months of basic living expenses",
                        status = "IN_PROGRESS"
                    )
                )
                goalDao.insertGoal(
                    SavingsGoalEntity(
                        name = "New Laptop",
                        targetAmount = BigDecimal("75000.00"),
                        currentAmount = BigDecimal("35000.00"),
                        targetDate = LocalDate.now().plusMonths(3).toString(),
                        description = "MacBook Air for productivity & freelance",
                        status = "IN_PROGRESS"
                    )
                )
            }
        }
    }
}
