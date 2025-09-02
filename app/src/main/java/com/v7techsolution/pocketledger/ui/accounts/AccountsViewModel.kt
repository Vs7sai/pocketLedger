package com.v7techsolution.pocketledger.ui.accounts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v7techsolution.pocketledger.data.entity.Account
import com.v7techsolution.pocketledger.data.manager.AccountManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountManager: AccountManager
) : ViewModel() {

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    private val _totalNetWorth = MutableLiveData<BigDecimal>()
    val totalNetWorth: LiveData<BigDecimal> = _totalNetWorth

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                accountManager.getAllActiveAccounts().collect { accounts ->
                    _accounts.value = accounts
                    calculateTotalNetWorth(accounts)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateTotalNetWorth(accounts: List<Account>) {
        val total = accounts.sumOf { it.balance }
        _totalNetWorth.value = total
    }

    fun addAccount(account: Account) {
        viewModelScope.launch {
            try {
                val accountId = accountManager.insertAccount(account)
                if (accountId > 0) {
                    loadAccounts() // Reload to refresh the list
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            try {
                accountManager.updateAccount(account)
                loadAccounts() // Reload to refresh the list
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            try {
                accountManager.deleteAccount(account)
                loadAccounts() // Reload to refresh the list
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun refreshData() {
        loadAccounts()
    }
}
