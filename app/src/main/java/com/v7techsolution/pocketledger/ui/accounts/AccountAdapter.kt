package com.v7techsolution.pocketledger.ui.accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.v7techsolution.pocketledger.data.entity.Account
import com.v7techsolution.pocketledger.databinding.ItemAccountBinding
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

class AccountAdapter(
    private val onEditClick: (Account) -> Unit,
    private val onDeleteClick: (Account) -> Unit
) : ListAdapter<Account, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AccountViewHolder(
        private val binding: ItemAccountBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(account: Account) {
            binding.apply {
                textViewAccountName.text = account.name
                textViewAccountType.text = account.type.name.replace("_", " ")
                textViewAccountIcon.text = account.icon
                textViewBalance.text = formatCurrency(account.balance)
                
                // Set account color
                root.setBackgroundColor(account.color)
                
                // Set text colors for better contrast
                val textColor = if (account.color.isDarkColor()) {
                    android.graphics.Color.WHITE
                } else {
                    android.graphics.Color.BLACK
                }
                textViewAccountName.setTextColor(textColor)
                textViewAccountType.setTextColor(textColor)
                textViewBalance.setTextColor(textColor)
                
                // Setup click listeners
                buttonEdit.setOnClickListener { onEditClick(account) }
                buttonDelete.setOnClickListener { onDeleteClick(account) }
            }
        }

        private fun formatCurrency(amount: BigDecimal): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            return formatter.format(amount)
        }
    }

    private fun Int.isDarkColor(): Boolean {
        val red = android.graphics.Color.red(this)
        val green = android.graphics.Color.green(this)
        val blue = android.graphics.Color.blue(this)
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
        return luminance < 0.5
    }

    private class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem == newItem
        }
    }
}
