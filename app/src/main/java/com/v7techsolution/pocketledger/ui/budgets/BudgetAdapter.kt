package com.v7techsolution.pocketledger.ui.budgets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.v7techsolution.pocketledger.data.entity.Budget
import com.v7techsolution.pocketledger.data.entity.Category
import com.v7techsolution.pocketledger.databinding.ItemBudgetBinding
import java.math.BigDecimal
import java.math.RoundingMode

data class BudgetWithCategory(
    val budget: Budget,
    val category: Category
)

class BudgetAdapter(
    private val budgets: List<BudgetWithCategory>,
    private val onBudgetClick: (Budget) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    inner class BudgetViewHolder(private val binding: ItemBudgetBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(budgetWithCategory: BudgetWithCategory) {
            val budget = budgetWithCategory.budget
            val category = budgetWithCategory.category
            
            binding.apply {
                textViewCategoryName.text = category.name
                textViewCategoryIcon.text = category.icon ?: "💰"
                textViewBudgetAmount.text = "$${budget.amount.setScale(2, RoundingMode.HALF_UP)}"
                
                // Calculate progress
                val spent = budget.spent
                val remaining = budget.amount.subtract(spent)
                val progressPercentage = if (budget.amount > BigDecimal.ZERO) {
                    (spent.divide(budget.amount, 4, RoundingMode.HALF_UP) * BigDecimal(100)).toInt()
                } else 0
                
                // Update progress bar width based on percentage
                val progressWidth = if (budget.amount > BigDecimal.ZERO) {
                    (spent.divide(budget.amount, 4, RoundingMode.HALF_UP) * BigDecimal(100)).toInt()
                } else 0
                
                val layoutParams = progressBar.layoutParams
                layoutParams.width = (itemView.width * progressWidth / 100).coerceAtLeast(0)
                progressBar.layoutParams = layoutParams
                
                // Update spent and remaining amounts
                textViewSpentAmount.text = "$${spent.setScale(2, RoundingMode.HALF_UP)}"
                textViewRemainingAmount.text = "$${remaining.setScale(2, RoundingMode.HALF_UP)}"
                
                // Set progress percentage
                textViewProgress.text = "$progressPercentage%"
                
                // Set progress badge color based on percentage
                val badgeColorRes = when {
                    progressPercentage >= 90 -> com.v7techsolution.pocketledger.R.color.negative_balance
                    progressPercentage >= 75 -> com.v7techsolution.pocketledger.R.color.warning_color
                    else -> com.v7techsolution.pocketledger.R.color.positive_balance
                }
                
                // Update progress badge background
                textViewProgress.background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 40f
                    setColor(itemView.context.getColor(badgeColorRes))
                }
                
                // Set click listeners
                buttonEdit.setOnClickListener {
                    onBudgetClick(budget)
                }
                
                buttonDelete.setOnClickListener {
                    // Handle delete - you can add a callback for this if needed
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(budgets[position])
    }

    override fun getItemCount(): Int = budgets.size
}
