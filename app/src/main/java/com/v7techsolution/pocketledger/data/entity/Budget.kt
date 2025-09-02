package com.v7techsolution.pocketledger.data.entity

import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.YearMonth

data class Budget(
    val id: Long = 0,
    val categoryId: Long,
    val amount: BigDecimal,
    val month: YearMonth,
    val spent: BigDecimal = BigDecimal.ZERO,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
