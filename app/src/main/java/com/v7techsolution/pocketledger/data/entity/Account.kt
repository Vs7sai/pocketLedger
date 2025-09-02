package com.v7techsolution.pocketledger.data.entity

import java.math.BigDecimal
import java.time.LocalDateTime

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: BigDecimal,
    val currency: String = "USD",
    val color: Int,
    val icon: String,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class AccountType {
    CASH, BANK_ACCOUNT, CREDIT_CARD, DEBIT_CARD, SAVINGS, INVESTMENT
}
