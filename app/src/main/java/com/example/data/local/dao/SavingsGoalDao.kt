package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SavingsContributionEntity
import com.example.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY id ASC")
    fun getAllGoalsFlow(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoalEntity)

    @Query("SELECT * FROM savings_goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Long): SavingsGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: SavingsContributionEntity): Long

    @Query("SELECT * FROM savings_contributions WHERE goalId = :goalId ORDER BY timestamp DESC")
    fun getContributionsForGoalFlow(goalId: Long): Flow<List<SavingsContributionEntity>>

    @Query("SELECT COUNT(*) FROM savings_goals")
    suspend fun getGoalCount(): Int
}
