package com.v7techsolution.pocketledger.data.entity

import java.time.LocalDateTime

data class Category(
    val id: Long = 0,
    val name: String,
    val color: Int,
    val icon: String,
    val type: CategoryType,
    val isDefault: Boolean = false,
    val usageCount: Int = 0,
    val lastUsed: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class CategoryType {
    INCOME, EXPENSE, TRANSFER
}
