package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "salary_profiles")
data class SalaryProfileEntity(
    @PrimaryKey val id: Long = 1L,
    val annualCtc: BigDecimal = BigDecimal("600000.00"),
    val monthlyTakeHome: BigDecimal = BigDecimal("42000.00"),
    val monthlyFixedExpenses: BigDecimal = BigDecimal("10000.00"),
    val monthlySavingsTarget: BigDecimal = BigDecimal("8000.00"),
    val salaryCreditDay: Int = 1,
    val currency: String = "INR",
    val timezone: String = "Asia/Kolkata",
    val effectiveDate: String = "2026-01-01"
)
