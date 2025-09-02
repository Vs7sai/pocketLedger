package com.v7techsolution.pocketledger.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.v7techsolution.pocketledger.data.entity.Transaction
import com.v7techsolution.pocketledger.data.entity.TransactionType
import com.v7techsolution.pocketledger.databinding.ItemTransactionBinding
import java.math.BigDecimal
import java.time.format.DateTimeFormatter
import java.util.*

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                textAmount.text = formatCurrency(transaction.amount, transaction.type)
                textCategory.text = transaction.category
                textDescription.text = transaction.description ?: ""
                textDate.text = formatDate(transaction.date)
                
                // Set color based on transaction type
                val colorRes = when (transaction.type) {
                    TransactionType.INCOME -> android.R.color.holo_green_dark
                    TransactionType.EXPENSE -> android.R.color.holo_red_dark
                    TransactionType.TRANSFER -> android.R.color.holo_blue_dark
                }
                textAmount.setTextColor(itemView.context.getColor(colorRes))
            }
        }

        private fun formatCurrency(amount: BigDecimal, type: TransactionType): String {
            val prefix = when (type) {
                TransactionType.INCOME -> "+"
                TransactionType.EXPENSE -> "-"
                TransactionType.TRANSFER -> ""
            }
            return "$prefix${String.format(Locale.getDefault(), "%.2f", amount)}"
        }

        private fun formatDate(date: java.time.LocalDateTime): String {
            val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
            return date.format(formatter)
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
