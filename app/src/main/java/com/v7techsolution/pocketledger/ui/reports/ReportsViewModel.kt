package com.v7techsolution.pocketledger.ui.reports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v7techsolution.pocketledger.data.manager.TransactionManager
import com.v7techsolution.pocketledger.data.manager.CategoryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionManager: TransactionManager,
    private val categoryManager: CategoryManager
) : ViewModel() {

    private val _monthlySummary = MutableLiveData<MonthlySummary>()
    val monthlySummary: LiveData<MonthlySummary> = _monthlySummary

    private val _categoryBreakdown = MutableLiveData<List<CategorySpending>>()
    val categoryBreakdown: LiveData<List<CategorySpending>> = _categoryBreakdown

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentMonth = YearMonth.now()

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                loadMonthlySummary()
                loadCategoryBreakdown()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadMonthlySummary() {
        val startDate = currentMonth.atDay(1).atStartOfDay()
        val endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59)
        
        val income = BigDecimal.valueOf(transactionManager.getTotalIncomeFromDate(startDate))
        val expenses = BigDecimal.valueOf(transactionManager.getTotalExpensesFromDate(startDate))
        val netAmount = income.subtract(expenses)
        
        val summary = MonthlySummary(
            month = currentMonth,
            totalIncome = income,
            totalExpenses = expenses,
            netAmount = netAmount
        )
        _monthlySummary.value = summary
    }

    private suspend fun loadCategoryBreakdown() {
        val startDate = currentMonth.atDay(1).atStartOfDay()
        
        // Get all expense categories and their spending
        val categories = categoryManager.getCategoriesByType(com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE)
        val breakdown = mutableListOf<CategorySpending>()
        
        categories.collect { categoryList ->
            categoryList.forEach { category ->
                val spending = BigDecimal.valueOf(transactionManager.getTotalExpensesFromDate(startDate))
                if (spending > BigDecimal.ZERO) {
                    breakdown.add(CategorySpending(category.name, spending))
                }
            }
        }
        
        // Sort by amount descending
        breakdown.sortByDescending { it.amount }
        _categoryBreakdown.value = breakdown
    }

    fun setCurrentMonth() {
        currentMonth = YearMonth.now()
        loadReports()
    }

    fun setPreviousMonth() {
        currentMonth = currentMonth.minusMonths(1)
        loadReports()
    }

    fun setNextMonth() {
        currentMonth = currentMonth.plusMonths(1)
        loadReports()
    }

    fun exportReport() {
        viewModelScope.launch {
            try {
                // Export logic would go here
                // For now, just log the action
                android.util.Log.d("ReportsViewModel", "Exporting report for ${currentMonth}")
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun refreshData() {
        loadReports()
    }
}

data class MonthlySummary(
    val month: YearMonth,
    val totalIncome: BigDecimal,
    val totalExpenses: BigDecimal,
    val netAmount: BigDecimal
)

data class CategorySpending(
    val categoryName: String,
    val amount: BigDecimal
)
