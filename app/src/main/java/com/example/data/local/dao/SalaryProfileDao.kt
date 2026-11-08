package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.SalaryProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaryProfileDao {
    @Query("SELECT * FROM salary_profiles WHERE id = 1 LIMIT 1")
    fun getSalaryProfileFlow(): Flow<SalaryProfileEntity?>

    @Query("SELECT * FROM salary_profiles WHERE id = 1 LIMIT 1")
    suspend fun getSalaryProfile(): SalaryProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: SalaryProfileEntity)
}
