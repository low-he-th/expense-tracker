package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "savings_contributions",
    foreignKeys = [
        ForeignKey(
            entity = SavingsGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["goalId"])]
)
data class SavingsContributionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val goalId: Long,
    val amount: BigDecimal,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
