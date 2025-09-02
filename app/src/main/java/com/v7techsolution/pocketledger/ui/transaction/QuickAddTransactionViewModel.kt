package com.v7techsolution.pocketledger.ui.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v7techsolution.pocketledger.data.entity.Account
import com.v7techsolution.pocketledger.data.entity.Category
import com.v7techsolution.pocketledger.data.entity.Transaction
import com.v7techsolution.pocketledger.data.entity.TransactionType
import com.v7techsolution.pocketledger.data.manager.AccountManager
import com.v7techsolution.pocketledger.data.manager.BudgetManager
import com.v7techsolution.pocketledger.data.manager.CategoryManager
import com.v7techsolution.pocketledger.data.manager.TransactionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class QuickAddTransactionViewModel @Inject constructor(
    private val transactionManager: TransactionManager,
    private val accountManager: AccountManager,
    private val categoryManager: CategoryManager,
    private val budgetManager: BudgetManager
) : ViewModel() {

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    private val _expenseCategories = MutableLiveData<List<Category>>()
    val expenseCategories: LiveData<List<Category>> = _expenseCategories

    private val _incomeCategories = MutableLiveData<List<Category>>()
    val incomeCategories: LiveData<List<Category>> = _incomeCategories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _transactionSaved = MutableLiveData<Boolean>()
    val transactionSaved: LiveData<Boolean> = _transactionSaved

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("QuickAddTransactionViewModel", "Starting to load data...")
                
                // Set hardcoded categories and accounts instantly - no database loading needed!
                _expenseCategories.value = getHardcodedExpenseCategories()
                _incomeCategories.value = getHardcodedIncomeCategories()
                _accounts.value = getHardcodedAccounts()
                android.util.Log.d("QuickAddTransactionViewModel", "Categories and accounts loaded instantly from frontend")
                
                // Set loading to false immediately - everything is ready!
                _isLoading.value = false
                android.util.Log.d("QuickAddTransactionViewModel", "Loading state set to false - everything ready instantly")
                
                android.util.Log.d("QuickAddTransactionViewModel", "Data loading completed successfully")
            } catch (e: Exception) {
                android.util.Log.e("QuickAddTransactionViewModel", "Error loading data", e)
                // Set empty lists on error to prevent crashes
                _expenseCategories.value = emptyList()
                _incomeCategories.value = emptyList()
                _accounts.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun saveTransaction(
        amount: BigDecimal,
        type: TransactionType,
        category: String,
        description: String?,
        accountId: Long,
        receiptPhotoPath: String?
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("QuickAddTransactionViewModel", "Saving transaction: amount=$amount, type=$type, category=$category, accountId=$accountId")
                
                // Quick validation - fail fast
                if (amount <= BigDecimal.ZERO || category.isBlank() || accountId <= 0) {
                    android.util.Log.e("QuickAddTransactionViewModel", "Validation failed")
                    _transactionSaved.postValue(false)
                    return@launch
                }
                
                // Create transaction object with minimal processing
                val transaction = Transaction(
                    id = 0,
                    amount = amount,
                    type = type,
                    category = category,
                    description = description,
                    accountId = accountId,
                    date = LocalDateTime.now(),
                    receiptPhotoPath = receiptPhotoPath,
                    isRecurring = false,
                    recurringPattern = null,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                // Save transaction - this is the main operation
                val transactionId = transactionManager.insertTransaction(transaction)
                
                if (transactionId > 0) {
                    // Set success immediately
                    _transactionSaved.postValue(true)
                    android.util.Log.d("QuickAddTransactionViewModel", "Transaction saved successfully with ID: $transactionId")
                    
                    // Update budget spending for expense transactions
                    if (type == TransactionType.EXPENSE) {
                        updateBudgetSpending(category, amount)
                    }
                } else {
                    android.util.Log.e("QuickAddTransactionViewModel", "Failed to save transaction")
                    _transactionSaved.postValue(false)
                }
            } catch (e: Exception) {
                android.util.Log.e("QuickAddTransactionViewModel", "Error saving transaction", e)
                _transactionSaved.postValue(false)
            }
        }
    }

    fun resetTransactionSaved() {
        _transactionSaved.value = false
    }
    
    // Clear the saved state when starting a new transaction
    fun clearSavedState() {
        _transactionSaved.value = false
    }
    
    // Update budget spending when expense transaction is added
    private suspend fun updateBudgetSpending(categoryName: String, amount: BigDecimal) {
        try {
            // Find the category ID for the category name
            val category = _expenseCategories.value?.find { it.name == categoryName }
            if (category != null) {
                // Get current month's budget for this category
                val currentMonth = java.time.YearMonth.now()
                val budgets = budgetManager.getBudgetsByMonth(currentMonth).first()
                val budget = budgets.find { it.categoryId == category.id }
                
                if (budget != null) {
                    // Update the spent amount
                    val newSpent = budget.spent.add(amount)
                    budgetManager.updateBudgetSpent(budget.id, newSpent)
                    android.util.Log.d("QuickAddTransactionViewModel", "Updated budget spending for ${category.name}: ${budget.spent} -> $newSpent")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("QuickAddTransactionViewModel", "Error updating budget spending", e)
        }
    }
    
    // Hardcoded expense categories - 10 most important ones
    private fun getHardcodedExpenseCategories(): List<Category> {
        return listOf(
            Category(id = 1, name = "Food & Dining", color = 0xFFE57373.toInt(), icon = "🍽️", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 2, name = "Groceries", color = 0xFF81C784.toInt(), icon = "🛒", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 3, name = "Transportation", color = 0xFF81C784.toInt(), icon = "🚗", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 4, name = "Gas & Fuel", color = 0xFF795548.toInt(), icon = "⛽", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 5, name = "Housing", color = 0xFFFF8A65.toInt(), icon = "🏠", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 6, name = "Rent", color = 0xFFFF5722.toInt(), icon = "🏘️", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 7, name = "Utilities", color = 0xFFBA68C8.toInt(), icon = "⚡", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 8, name = "Internet", color = 0xFF3F51B5.toInt(), icon = "🌐", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 9, name = "Healthcare", color = 0xFFF06292.toInt(), icon = "🏥", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 10, name = "Shopping", color = 0xFF64B5F6.toInt(), icon = "🛍️", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now())
        )
    }
    
    // Hardcoded income categories - 10 most important ones
    private fun getHardcodedIncomeCategories(): List<Category> {
        return listOf(
            Category(id = 11, name = "Salary", color = 0xFF4DB6AC.toInt(), icon = "💰", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 12, name = "Hourly Wages", color = 0xFF4CAF50.toInt(), icon = "⏰", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 13, name = "Freelance", color = 0xFF81C784.toInt(), icon = "💼", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 14, name = "Business Income", color = 0xFF4CAF50.toInt(), icon = "🏢", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 15, name = "Investment Returns", color = 0xFF4DB6AC.toInt(), icon = "📈", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 16, name = "Rental Income", color = 0xFF81C784.toInt(), icon = "🏠", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 17, name = "Side Hustle", color = 0xFFFFB74D.toInt(), icon = "🚀", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 18, name = "Bonus", color = 0xFF4CAF50.toInt(), icon = "🎯", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 19, name = "Tips", color = 0xFFFF9800.toInt(), icon = "💡", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 20, name = "Refunds", color = 0xFF4DB6AC.toInt(), icon = "↩️", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now())
        )
    }
    
    // Hardcoded accounts - most common account types
    private fun getHardcodedAccounts(): List<Account> {
        return listOf(
            Account(id = 1, name = "Cash", type = com.v7techsolution.pocketledger.data.entity.AccountType.CASH, balance = java.math.BigDecimal("1000.00"), color = 0xFF4CAF50.toInt(), icon = "💵", isActive = true, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Account(id = 2, name = "Checking Account", type = com.v7techsolution.pocketledger.data.entity.AccountType.BANK_ACCOUNT, balance = java.math.BigDecimal("2500.00"), color = 0xFF2196F3.toInt(), icon = "🏦", isActive = true, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Account(id = 3, name = "Savings Account", type = com.v7techsolution.pocketledger.data.entity.AccountType.SAVINGS, balance = java.math.BigDecimal("5000.00"), color = 0xFF4CAF50.toInt(), icon = "💰", isActive = true, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Account(id = 4, name = "Credit Card", type = com.v7techsolution.pocketledger.data.entity.AccountType.CREDIT_CARD, balance = java.math.BigDecimal("-500.00"), color = 0xFFF44336.toInt(), icon = "💳", isActive = true, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now())
        )
    }
}
