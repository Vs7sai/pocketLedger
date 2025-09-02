package com.v7techsolution.pocketledger.data.entity

import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentReminder(
    val id: Long = 0,
    val title: String,
    val amount: BigDecimal,
    val dueDate: LocalDateTime,
    val categoryId: Long,
    val accountId: Long,
    val description: String? = null,
    val isRecurring: Boolean = false,
    val recurringPattern: String? = null,
    val reminderDays: Int = 3,
    val isActive: Boolean = true,
    val isPaid: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
