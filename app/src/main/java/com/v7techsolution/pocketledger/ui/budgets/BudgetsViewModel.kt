package com.v7techsolution.pocketledger.ui.budgets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v7techsolution.pocketledger.data.entity.Budget
import com.v7techsolution.pocketledger.data.entity.Category
import com.v7techsolution.pocketledger.data.manager.BudgetManager
import com.v7techsolution.pocketledger.data.manager.CategoryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetManager: BudgetManager,
    private val categoryManager: CategoryManager
) : ViewModel() {

    private val _budgets = MutableLiveData<List<Budget>>()
    val budgets: LiveData<List<Budget>> = _budgets

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _totalBudgeted = MutableLiveData<BigDecimal>()
    val totalBudgeted: LiveData<BigDecimal> = _totalBudgeted

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentMonth = YearMonth.now()

    init {
        loadCategories()
        loadBudgets()
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                // Use hardcoded categories for both income and expense
                _categories.value = getHardcodedAllCategories()
            } catch (e: Exception) {
                android.util.Log.e("BudgetsViewModel", "Error loading categories", e)
                _categories.value = emptyList()
            }
        }
    }

    fun loadBudgets() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                budgetManager.getBudgetsByMonth(currentMonth).collect { budgets ->
                    _budgets.value = budgets
                    calculateTotalBudgeted(budgets)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateTotalBudgeted(budgets: List<Budget>) {
        val total = budgets.sumOf { it.amount }
        _totalBudgeted.value = total
    }

    fun addBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                val budgetId = budgetManager.insertBudget(budget)
                if (budgetId > 0) {
                    loadBudgets() // Reload to refresh the list
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                budgetManager.updateBudget(budget)
                loadBudgets() // Reload to refresh the list
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                budgetManager.deleteBudget(budget)
                loadBudgets() // Reload to refresh the list
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun setCurrentMonth() {
        currentMonth = YearMonth.now()
        loadBudgets()
    }

    fun setMonth(yearMonth: YearMonth) {
        currentMonth = yearMonth
        loadBudgets()
    }

    fun refreshData() {
        loadBudgets()
    }
    
    // Hardcoded BUDGET-SPECIFIC categories - different from transaction categories
    private fun getHardcodedAllCategories(): List<Category> {
        return listOf(
            // BUDGET-SPECIFIC EXPENSE CATEGORIES (Different from transaction categories)
            Category(id = 101, name = "Monthly Bills", color = 0xFFE57373.toInt(), icon = "📋", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 102, name = "Entertainment", color = 0xFF81C784.toInt(), icon = "🎬", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 103, name = "Personal Care", color = 0xFF81C784.toInt(), icon = "💄", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 104, name = "Education", color = 0xFF795548.toInt(), icon = "📚", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 105, name = "Insurance", color = 0xFFFF8A65.toInt(), icon = "🛡️", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 106, name = "Debt Payment", color = 0xFFFF5722.toInt(), icon = "💳", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 107, name = "Emergency Fund", color = 0xFFBA68C8.toInt(), icon = "🚨", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 108, name = "Travel", color = 0xFF3F51B5.toInt(), icon = "✈️", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 109, name = "Fitness", color = 0xFFF06292.toInt(), icon = "💪", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 110, name = "Hobbies", color = 0xFF64B5F6.toInt(), icon = "🎨", type = com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            
            // BUDGET-SPECIFIC INCOME CATEGORIES (Different from transaction categories)
            Category(id = 201, name = "Monthly Salary", color = 0xFF4CAF50.toInt(), icon = "💼", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 202, name = "Commission", color = 0xFF2196F3.toInt(), icon = "📊", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 203, name = "Dividends", color = 0xFF9C27B0.toInt(), icon = "📈", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 204, name = "Interest", color = 0xFFFF9800.toInt(), icon = "💰", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 205, name = "Pension", color = 0xFF4CAF50.toInt(), icon = "👴", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 206, name = "Social Security", color = 0xFFE91E63.toInt(), icon = "🏛️", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 207, name = "Child Support", color = 0xFF607D8B.toInt(), icon = "👶", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 208, name = "Alimony", color = 0xFF795548.toInt(), icon = "💔", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 209, name = "Scholarship", color = 0xFF009688.toInt(), icon = "🎓", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now()),
            Category(id = 210, name = "Grant", color = 0xFF9E9E9E.toInt(), icon = "🏆", type = com.v7techsolution.pocketledger.data.entity.CategoryType.INCOME, isDefault = true, usageCount = 0, lastUsed = null, createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now())
        )
    }
}
