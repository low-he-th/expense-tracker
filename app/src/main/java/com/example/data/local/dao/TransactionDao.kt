package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE transactionDate = :date ORDER BY timestamp DESC")
    fun getTransactionsByDateFlow(date: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE transactionDate = :date ORDER BY timestamp DESC")
    suspend fun getTransactionsByDate(date: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE transactionDate BETWEEN :startDate AND :endDate ORDER BY transactionDate DESC, timestamp DESC")
    fun getTransactionsBetweenDatesFlow(startDate: String, endDate: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int
}
