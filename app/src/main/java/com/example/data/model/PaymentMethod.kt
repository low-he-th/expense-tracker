package com.example.data.model

enum class PaymentMethod(val label: String) {
    UPI("UPI"),
    CASH("Cash"),
    DEBIT_CARD("Debit Card"),
    CREDIT_CARD("Credit Card"),
    OTHER("Other");

    companion object {
        fun fromString(value: String?): PaymentMethod {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
                ?: UPI
        }
    }
}
