package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal = BigDecimal.ZERO,
    val targetDate: String = "",
    val description: String = "",
    val status: String = "IN_PROGRESS"
)
