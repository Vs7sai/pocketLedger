package com.v7techsolution.pocketledger.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v7techsolution.pocketledger.data.entity.Budget
import com.v7techsolution.pocketledger.data.entity.Transaction
import com.v7techsolution.pocketledger.data.manager.BudgetManager
import com.v7techsolution.pocketledger.data.manager.TransactionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

data class BudgetWarning(
    val categoryName: String,
    val budgetAmount: BigDecimal,
    val spentAmount: BigDecimal,
    val remainingAmount: BigDecimal,
    val percentageUsed: Double,
    val isOverBudget: Boolean
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionManager: TransactionManager,
    private val budgetManager: BudgetManager
) : ViewModel() {

    private val _todayBalance = MutableLiveData<BigDecimal>()
    val todayBalance: LiveData<BigDecimal> = _todayBalance

    private val _monthlyIncome = MutableLiveData<BigDecimal>()
    val monthlyIncome: LiveData<BigDecimal> = _monthlyIncome

    private val _monthlyExpenses = MutableLiveData<BigDecimal>()
    val monthlyExpenses: LiveData<BigDecimal> = _monthlyExpenses

    private val _netIncome = MutableLiveData<BigDecimal>()
    val netIncome: LiveData<BigDecimal> = _netIncome

    private val _totalTransactions = MutableLiveData<Int>()
    val totalTransactions: LiveData<Int> = _totalTransactions

    private val _recentTransactions = MutableLiveData<List<Transaction>>()
    val recentTransactions: LiveData<List<Transaction>> = _recentTransactions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _budgetWarnings = MutableLiveData<List<BudgetWarning>>()
    val budgetWarnings: LiveData<List<BudgetWarning>> = _budgetWarnings

    fun loadTodayData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                loadTodayBalance()
                loadMonthlyData()
                loadRecentTransactions()
                loadTotalTransactions()
                loadBudgetWarnings()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadTodayBalance() {
        val today = LocalDateTime.now()
        val startOfDay = today.toLocalDate().atStartOfDay()
        val endOfDay = today.toLocalDate().atTime(23, 59, 59)

        val totalIncome = transactionManager.getTotalIncomeFromDate(startOfDay)
        val totalExpenses = transactionManager.getTotalExpensesFromDate(startOfDay)
        
        val balance = BigDecimal.valueOf(totalIncome - totalExpenses)
        _todayBalance.value = balance
    }

    private suspend fun loadMonthlyData() {
        val currentMonth = YearMonth.now()
        val startOfMonth = currentMonth.atDay(1).atStartOfDay()
        val endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59)

        val monthlyIncome = transactionManager.getTotalIncomeFromDate(startOfMonth)
        val monthlyExpenses = transactionManager.getTotalExpensesFromDate(startOfMonth)

        _monthlyIncome.value = BigDecimal.valueOf(monthlyIncome)
        _monthlyExpenses.value = BigDecimal.valueOf(monthlyExpenses)
        _netIncome.value = BigDecimal.valueOf(monthlyIncome - monthlyExpenses)
    }

    private suspend fun loadRecentTransactions() {
        val transactions = transactionManager.getRecentTransactions(10) // Get last 10 transactions
        _recentTransactions.value = transactions
    }

    private suspend fun loadTotalTransactions() {
        val total = transactionManager.getTotalTransactionCount()
        _totalTransactions.value = total
    }

    private suspend fun loadBudgetWarnings() {
        try {
            val currentMonth = YearMonth.now()
            val budgets = budgetManager.getBudgetsByMonth(currentMonth).first()
            
            val warnings = budgets.mapNotNull { budget: Budget ->
                val percentageUsed = if (budget.amount > BigDecimal.ZERO) {
                    (budget.spent.toDouble() / budget.amount.toDouble()) * 100
                } else 0.0
                
                val remainingAmount = budget.amount.subtract(budget.spent)
                val isOverBudget = budget.spent > budget.amount
                
                // Only show warnings for budgets that are 80% used or over budget
                if (percentageUsed >= 80.0 || isOverBudget) {
                    BudgetWarning(
                        categoryName = "Category ${budget.categoryId}", // We'll need to get category name
                        budgetAmount = budget.amount,
                        spentAmount = budget.spent,
                        remainingAmount = remainingAmount,
                        percentageUsed = percentageUsed,
                        isOverBudget = isOverBudget
                    )
                } else null
            }
            
            _budgetWarnings.value = warnings
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Error loading budget warnings", e)
            _budgetWarnings.value = emptyList()
        }
    }

    fun refreshData() {
        loadTodayData()
    }
}
