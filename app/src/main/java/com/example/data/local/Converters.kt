package com.example.data.local

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toPlainString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return if (value.isNullOrBlank()) BigDecimal.ZERO else BigDecimal(value)
    }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return if (value.isNullOrBlank()) LocalDate.now() else LocalDate.parse(value)
    }
}
