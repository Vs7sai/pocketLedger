package com.v7techsolution.pocketledger.data.entity

import java.math.BigDecimal
import java.time.LocalDateTime

data class Transaction(
    val id: Long = 0,
    val amount: BigDecimal,
    val type: TransactionType,
    val category: String,
    val description: String?,
    val accountId: Long,
    val date: LocalDateTime,
    val receiptPhotoPath: String?,
    val isRecurring: Boolean = false,
    val recurringPattern: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}
