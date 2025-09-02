package com.v7techsolution.pocketledger.ui.transaction

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.v7techsolution.pocketledger.R
import com.v7techsolution.pocketledger.data.entity.Account
import com.v7techsolution.pocketledger.data.entity.Category
import com.v7techsolution.pocketledger.data.entity.TransactionType
import com.v7techsolution.pocketledger.databinding.DialogQuickAddTransactionBinding
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.math.RoundingMode

@AndroidEntryPoint
class QuickAddTransactionDialog : DialogFragment() {

    private var _binding: DialogQuickAddTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuickAddTransactionViewModel by viewModels()
    
    // Callback interface for transaction saved
    interface TransactionSavedCallback {
        fun onTransactionSaved()
    }
    
    private var transactionSavedCallback: TransactionSavedCallback? = null
    
    fun setTransactionSavedCallback(callback: TransactionSavedCallback) {
        this.transactionSavedCallback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogQuickAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Clear any previous state when dialog opens
        viewModel.clearSavedState()
        
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup transaction type toggle
        binding.transactionTypeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnExpense -> {
                        // Switch to expense categories
                        val expenseCategories = viewModel.expenseCategories.value ?: emptyList()
                        setupCategoryChips(expenseCategories, TransactionType.EXPENSE)
                        binding.amountInput.hint = "Enter expense amount"
                    }
                    R.id.btnIncome -> {
                        // Switch to income categories
                        val incomeCategories = viewModel.incomeCategories.value ?: emptyList()
                        setupCategoryChips(incomeCategories, TransactionType.INCOME)
                        binding.amountInput.hint = "Enter income amount"
                    }
                }
            }
        }

        // Set default to expense and show expense categories
        binding.transactionTypeToggle.check(R.id.btnExpense)
        val expenseCategories = viewModel.expenseCategories.value ?: emptyList()
        setupCategoryChips(expenseCategories, TransactionType.EXPENSE)
        binding.amountInput.hint = "Enter expense amount"

        // Setup save button
        binding.btnSave.setOnClickListener {
            // Prevent multiple submissions
            binding.btnSave.isEnabled = false
            binding.btnSave.text = "Saving..."
            saveTransaction()
        }

        // Setup cancel button - properly dismiss the dialog
        binding.btnCancel.setOnClickListener {
            try {
                android.util.Log.d("QuickAddTransactionDialog", "Cancel button clicked - dismissing dialog")
                dismiss()
            } catch (e: Exception) {
                android.util.Log.e("QuickAddTransactionDialog", "Error dismissing dialog", e)
                // Fallback: try to close the activity
                activity?.finish()
            }
        }
        
        // Setup close button for success state - properly dismiss the dialog
        binding.btnClose.setOnClickListener {
            try {
                android.util.Log.d("QuickAddTransactionDialog", "Close button clicked - dismissing dialog")
                dismiss()
            } catch (e: Exception) {
                android.util.Log.e("QuickAddTransactionDialog", "Error dismissing dialog", e)
                // Fallback: try to close the activity
                activity?.finish()
            }
        }
        
        // Setup add another button
        binding.btnAddAnother.setOnClickListener {
            resetDialogForNewTransaction()
        }

        // Set default to expense
        binding.transactionTypeToggle.check(R.id.btnExpense)
    }

    private fun observeViewModel() {
        // Observe accounts
        viewModel.accounts.observe(viewLifecycleOwner, Observer { accounts ->
            setupAccountSpinner(accounts)
        })

        // Observe expense categories
        viewModel.expenseCategories.observe(viewLifecycleOwner, Observer { categories ->
            android.util.Log.d("QuickAddTransactionDialog", "Expense categories loaded: ${categories.size} categories")
            categories.forEach { cat -> android.util.Log.d("QuickAddTransactionDialog", "Expense: ${cat.name} (${cat.type})") }
            setupCategoryChips(categories, TransactionType.EXPENSE)
        })

        // Observe income categories
        viewModel.incomeCategories.observe(viewLifecycleOwner, Observer { categories ->
            android.util.Log.d("QuickAddTransactionDialog", "Income categories loaded: ${categories.size} categories")
            categories.forEach { cat -> android.util.Log.d("QuickAddTransactionDialog", "Income: ${cat.name} (${cat.type})") }
            setupCategoryChips(categories, TransactionType.INCOME)
        })

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        // Observe transaction saved - simplified to reduce binder traffic
        viewModel.transactionSaved.observe(viewLifecycleOwner, Observer { saved ->
            // Only show messages if we're in a save attempt state
            if (binding.btnSave.text == "Saving...") {
                if (saved) {
                    // Reset button state
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save"
                    
                    // Show success message
                    binding.successMessage.visibility = View.VISIBLE
                    
                    // Hide input fields
                    binding.transactionTypeToggle.visibility = View.GONE
                    binding.amountInputLayout.visibility = View.GONE
                    binding.accountInputLayout.visibility = View.GONE
                    binding.categoryChipGroup.visibility = View.GONE
                    binding.descriptionInputLayout.visibility = View.GONE
                    binding.btnSave.visibility = View.GONE
                    binding.btnCancel.visibility = View.GONE
                    
                    // Notify parent fragment
                    notifyTransactionSaved()
                    
                    // Auto-close after 3 seconds (reduced from 5)
                    binding.root.postDelayed({
                        if (isAdded && !isDetached && !isRemoving) {
                            dismiss()
                        }
                    }, 3000)
                    
                } else {
                    // Reset button state on failure
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save"
                    Toast.makeText(context, "Failed to save transaction", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupAccountSpinner(accounts: List<Account>) {
        val accountNames = accounts.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, accountNames)
        binding.accountSpinner.setAdapter(adapter)
        // Set default selection
        if (accounts.isNotEmpty()) {
            binding.accountSpinner.setText(accounts[0].name, false)
        }
    }

    private fun setupCategoryChips(categories: List<Category>, type: TransactionType) {
        try {
            android.util.Log.d("QuickAddTransactionDialog", "Setting up category chips for type: ${type.name}")
            android.util.Log.d("QuickAddTransactionDialog", "Total categories received: ${categories.size}")
            
            binding.categoryChipGroup.removeAllViews()
            
            // Filter categories by type to ensure we only show the right ones
            val filteredCategories = categories.filter { it.type.name == type.name }
            android.util.Log.d("QuickAddTransactionDialog", "Filtered categories for ${type.name}: ${filteredCategories.size}")
            filteredCategories.forEach { cat -> android.util.Log.d("QuickAddTransactionDialog", "Filtered: ${cat.name} (${cat.type})") }
            
            // Sort categories by usage count (most used first) and then alphabetically
            val sortedCategories = filteredCategories.sortedWith(
                compareByDescending<Category> { it.usageCount }
                .thenBy { it.name }
            )
            
            sortedCategories.forEach { category ->
                val chip = Chip(requireContext()).apply {
                    text = "${category.icon} ${category.name}"
                    isCheckable = true
                    
                    // Simple and stable styling for category selection
                    setChipBackgroundColorResource(R.color.chip_background_unselected)
                    setTextColor(resources.getColor(R.color.chip_text_color, null))
                    
                    // Simple stroke for better definition
                    chipStrokeWidth = 1f
                    chipStrokeColor = resources.getColorStateList(R.color.outline, null)
                    
                    // Enhanced color state list for better selection feedback
                    chipBackgroundColor = android.content.res.ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf(android.R.attr.state_pressed),
                            intArrayOf()
                        ),
                        intArrayOf(
                            resources.getColor(R.color.primary, null), // Selected: Primary blue
                            resources.getColor(R.color.primary_light, null), // Pressed: Light blue
                            resources.getColor(R.color.chip_background_unselected, null) // Default: Light gray
                        )
                    )
                    
                    // Enhanced text color for better contrast
                    setTextColor(android.content.res.ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf()
                        ),
                        intArrayOf(
                            resources.getColor(R.color.white, null), // Selected: White text
                            resources.getColor(R.color.chip_text_color, null) // Default: Dark text
                        )
                    ))
                    
                    // Set chip icon if available
                    if (category.icon.isNotEmpty()) {
                        chipIcon = null // We're already showing icon in text
                    }
                    
                    // Add elevation for better visual depth
                    elevation = 2f
                    
                    // Add ripple effect for better touch feedback
                    setRippleColorResource(R.color.primary_light)
                    
                    // Enhanced click listener with satisfying feedback
                    setOnClickListener {
                        try {
                            // Clear all other selections first
                            binding.categoryChipGroup.clearCheck()
                            // Check this chip
                            isChecked = true
                            
                            // Add satisfying visual feedback
                            android.util.Log.d("QuickAddTransactionDialog", "Category selected: ${category.name}")
                            
                            // Brief satisfying animation
                            scaleX = 0.95f
                            scaleY = 0.95f
                            postDelayed({
                                scaleX = 1.0f
                                scaleY = 1.0f
                            }, 100)
                            
                            // Update the amount input hint to show selected category with confirmation
                            val type = when (binding.transactionTypeToggle.checkedButtonId) {
                                R.id.btnExpense -> "expense"
                                R.id.btnIncome -> "income"
                                else -> "expense"
                            }
                            binding.amountInput.hint = "✅ Enter ${type} amount for ${category.name}"
                            
                            // Show a brief success feedback
                            binding.amountInput.requestFocus()
                            
                            // Add a subtle success animation to the amount input
                            binding.amountInputLayout.startIconDrawable = resources.getDrawable(R.drawable.ic_check, null)
                            binding.amountInputLayout.setStartIconTintList(resources.getColorStateList(R.color.positive_balance, null))
                            
                        } catch (e: Exception) {
                            android.util.Log.e("QuickAddTransactionDialog", "Error in category click", e)
                        }
                    }
                }
                binding.categoryChipGroup.addView(chip)
            }
            
            // Auto-select the first category for better UX
            if (sortedCategories.isNotEmpty()) {
                binding.categoryChipGroup.check(binding.categoryChipGroup.getChildAt(0).id)
                android.util.Log.d("QuickAddTransactionDialog", "Auto-selected first category: ${sortedCategories[0].name}")
            }
        } catch (e: Exception) {
            android.util.Log.e("QuickAddTransactionDialog", "Error setting up category chips", e)
        }
    }

    private fun saveTransaction() {
        // Quick validation - fail fast
        val amountText = binding.amountInput.text.toString()
        if (amountText.isEmpty()) {
            binding.amountInput.error = "Please enter an amount"
            return
        }

        val amount = try {
            BigDecimal(amountText).setScale(2, RoundingMode.HALF_UP)
        } catch (e: NumberFormatException) {
            binding.amountInput.error = "Invalid amount"
            return
        }

        val type = when (binding.transactionTypeToggle.checkedButtonId) {
            R.id.btnExpense -> TransactionType.EXPENSE
            R.id.btnIncome -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }

        val selectedCategory = binding.categoryChipGroup.checkedChipId
        val categoryName = if (selectedCategory == View.NO_ID) {
            // No category selected - use default based on transaction type
            when (type) {
                TransactionType.EXPENSE -> "Food & Dining" // Default expense category
                TransactionType.INCOME -> "Salary" // Default income category
                else -> "Food & Dining"
            }
        } else {
            val categoryChip = binding.categoryChipGroup.findViewById<Chip>(selectedCategory)
            if (categoryChip == null) {
                // Fallback to default if chip is null
                when (type) {
                    TransactionType.EXPENSE -> "Food & Dining"
                    TransactionType.INCOME -> "Salary"
                    else -> "Food & Dining"
                }
            } else {
                categoryChip.text.toString().substringAfter(" ")
            }
        }

        val description = binding.descriptionInput.text.toString().takeIf { it.isNotEmpty() }
        
        // Get account ID from selected account name - optimized lookup with default fallback
        val selectedAccountName = binding.accountSpinner.text.toString()
        val accountId = if (selectedAccountName.isNotEmpty()) {
            viewModel.accounts.value?.find { it.name == selectedAccountName }?.id ?: 1L
        } else {
            // No account selected - use first available account or default
            viewModel.accounts.value?.firstOrNull()?.id ?: 1L
        }

        // Save transaction - this is now much faster
        viewModel.saveTransaction(
            amount = amount,
            type = type,
            category = categoryName,
            description = description,
            accountId = accountId,
            receiptPhotoPath = null
        )
        
        // Quick timeout for faster response
        binding.btnSave.postDelayed({
            binding.btnSave.isEnabled = true
            binding.btnSave.text = "Save"
        }, 1000) // Reduced to 1 second
    }

    private fun notifyTransactionSaved() {
        try {
            // Use callback to notify parent fragment to refresh data
            transactionSavedCallback?.onTransactionSaved()
            android.util.Log.d("QuickAddTransactionDialog", "Transaction saved callback executed successfully")
        } catch (e: Exception) {
            android.util.Log.e("QuickAddTransactionDialog", "Error executing transaction saved callback", e)
        }
    }
    
    private fun resetDialogForNewTransaction() {
        try {
            android.util.Log.d("QuickAddTransactionDialog", "Resetting dialog for new transaction")
            
            // Clear all input fields
            binding.amountInput.text?.clear()
            binding.descriptionInput.text?.clear()
            binding.categoryChipGroup.clearCheck()
            binding.accountSpinner.text?.clear()
            
            // Reset transaction type to expense
            binding.transactionTypeToggle.check(R.id.btnExpense)
            
            // Show all input fields again
            binding.transactionTypeToggle.visibility = View.VISIBLE
            binding.amountInputLayout.visibility = View.VISIBLE
            binding.accountInputLayout.visibility = View.VISIBLE
            binding.categoryChipGroup.visibility = View.VISIBLE
            binding.descriptionInputLayout.visibility = View.VISIBLE
            binding.btnSave.visibility = View.VISIBLE
            binding.btnCancel.visibility = View.VISIBLE
            
            // Hide success message
            binding.successMessage.visibility = View.GONE
            
            // Reset button state
            binding.btnSave.isEnabled = true
            binding.btnSave.text = "Save"
            
            // Clear saved state
            viewModel.clearSavedState()
            
            android.util.Log.d("QuickAddTransactionDialog", "Dialog reset successfully for new transaction")
            
        } catch (e: Exception) {
            android.util.Log.e("QuickAddTransactionDialog", "Error resetting dialog", e)
        }
    }
    
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        android.util.Log.d("QuickAddTransactionDialog", "Dialog dismissed successfully")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("QuickAddTransactionDialog", "Dialog destroyed successfully")
    }
    
    // Override to allow normal dialog behavior
    override fun onCancel(dialog: DialogInterface) {
        android.util.Log.d("QuickAddTransactionDialog", "Dialog cancel - allowing normal behavior")
        super.onCancel(dialog)
    }
    
    // Override to allow normal dialog behavior
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        
        // Allow normal dialog behavior
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        
        return dialog
    }
    
    // Override to allow normal dialog behavior
    override fun onResume() {
        super.onResume()
        // Allow normal dialog behavior
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
