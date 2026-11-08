package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["transactionDate"]),
        Index(value = ["idempotencyKey"], unique = true)
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amount: BigDecimal,
    val category: String,
    val description: String = "",
    val paymentMethod: String = "UPI",
    val transactionDate: String, // yyyy-MM-dd
    val timestamp: Long = System.currentTimeMillis(),
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)
