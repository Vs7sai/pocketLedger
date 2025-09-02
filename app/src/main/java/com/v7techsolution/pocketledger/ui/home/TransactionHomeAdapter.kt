package com.v7techsolution.pocketledger.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.v7techsolution.pocketledger.data.entity.Transaction
import com.v7techsolution.pocketledger.databinding.ItemTransactionHomeBinding
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TransactionHomeAdapter : ListAdapter<Transaction, TransactionHomeAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionHomeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionHomeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                // Set category icon (using emoji for now)
                textViewCategoryIcon.text = getCategoryIcon(transaction.category)
                
                // Set category name
                textViewCategoryName.text = getCategoryName(transaction.category)
                
                // Set transaction note
                textViewTransactionNote.text = transaction.description ?: "No note"
                
                // Set transaction date
                textViewTransactionDate.text = formatTransactionDate(transaction.date)
                
                // Set amount with proper formatting and color
                val amount = transaction.amount
                val isExpense = transaction.type == com.v7techsolution.pocketledger.data.entity.TransactionType.EXPENSE
                val displayAmount = if (isExpense) amount else amount.negate()
                
                textViewAmount.text = formatCurrency(displayAmount)
                
                // Set amount color based on transaction type
                val colorRes = when {
                    isExpense -> com.v7techsolution.pocketledger.R.color.expense_icon
                    else -> com.v7techsolution.pocketledger.R.color.income_icon
                }
                textViewAmount.setTextColor(binding.root.context.getColor(colorRes))
                
                // Set account name
                textViewAccountName.text = getAccountName(transaction.accountId)
            }
        }

        private fun getCategoryIcon(category: String): String {
            // For now, return a default icon. In a real app, you'd fetch this from the category
            return "💰"
        }

        private fun getCategoryName(category: String): String {
            // For now, return the category name directly
            return category
        }

        private fun getAccountName(accountId: Long): String {
            // For now, return a default name. In a real app, you'd fetch this from the account
            return "Account"
        }

        private fun formatTransactionDate(date: LocalDateTime): String {
            val now = LocalDateTime.now()
            val today = now.toLocalDate()
            val transactionDate = date.toLocalDate()
            
            return when {
                transactionDate == today -> "Today, ${formatTime(date)}"
                transactionDate == today.minusDays(1) -> "Yesterday, ${formatTime(date)}"
                else -> {
                    val formatter = DateTimeFormatter.ofPattern("MMM dd, ${formatTime(date)}")
                    date.format(formatter)
                }
            }
        }

        private fun formatTime(date: LocalDateTime): String {
            val formatter = DateTimeFormatter.ofPattern("h:mm a")
            return date.format(formatter)
        }

        private fun formatCurrency(amount: BigDecimal): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            return formatter.format(amount.abs())
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
