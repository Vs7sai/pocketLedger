package com.v7techsolution.pocketledger.ui.budgets

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.v7techsolution.pocketledger.data.entity.Budget
import com.v7techsolution.pocketledger.data.entity.Category
import com.v7techsolution.pocketledger.databinding.DialogAddEditBudgetBinding
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.YearMonth

@AndroidEntryPoint
class AddEditBudgetDialog : DialogFragment() {

    private var _binding: DialogAddEditBudgetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetsViewModel by viewModels()
    private var editingBudget: Budget? = null

    companion object {
        fun newInstance(budget: Budget): AddEditBudgetDialog {
            return AddEditBudgetDialog().apply {
                editingBudget = budget
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddEditBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupObservers()
        
        // Set title based on mode
        binding.textViewTitle.text = if (editingBudget == null) "Add Budget" else "Edit Budget"
    }

    private fun setupObservers() {
        // Observe categories and setup spinner when they're loaded
        viewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
            setupCategorySpinner(categories)
            populateFieldsIfEditing()
        })
    }

    private fun setupCategorySpinner(categories: List<Category>) {
        // Filter to show only EXPENSE categories for budgets (budgets are for spending limits)
        val expenseCategories = categories.filter { it.type == com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE }
        val categoryNames = expenseCategories.map { "${it.icon} ${it.name}" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
        binding.spinnerCategory.setAdapter(adapter)
        binding.spinnerCategory.threshold = 0 // Show dropdown immediately when clicked
    }

    private fun setupClickListeners() {
        binding.buttonSave.setOnClickListener {
            saveBudget()
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun populateFieldsIfEditing() {
        editingBudget?.let { budget ->
            binding.editTextAmount.setText(budget.amount.toString())
            
            // Set category - only from expense categories
            val categories = viewModel.categories.value ?: emptyList()
            val expenseCategories = categories.filter { it.type == com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE }
            val category = expenseCategories.find { it.id == budget.categoryId }
            if (category != null) {
                binding.spinnerCategory.setText("${category.icon} ${category.name}", false)
            }
        }
    }

    private fun saveBudget() {
        val amountText = binding.editTextAmount.text.toString().trim()
        val selectedCategoryText = binding.spinnerCategory.text.toString().trim()
        val categories = viewModel.categories.value ?: emptyList()

        // Validation
        if (amountText.isEmpty()) {
            binding.editTextAmount.error = "Amount is required"
            return
        }

        val amount = try {
            BigDecimal(amountText).setScale(2, RoundingMode.HALF_UP)
        } catch (e: NumberFormatException) {
            binding.editTextAmount.error = "Invalid amount"
            return
        }

        if (selectedCategoryText.isEmpty()) {
            Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        // Find category by name (remove emoji prefix) - only from expense categories
        val expenseCategories = categories.filter { it.type == com.v7techsolution.pocketledger.data.entity.CategoryType.EXPENSE }
        val selectedCategory = expenseCategories.find { category ->
            "${category.icon} ${category.name}" == selectedCategoryText
        }

        if (selectedCategory == null) {
            Toast.makeText(context, "Please select a valid category", Toast.LENGTH_SHORT).show()
            return
        }
        val currentMonth = YearMonth.now()

        val budget = if (editingBudget != null) {
            editingBudget!!.copy(
                amount = amount,
                categoryId = selectedCategory.id,
                updatedAt = LocalDateTime.now()
            )
        } else {
            Budget(
                categoryId = selectedCategory.id,
                amount = amount,
                month = currentMonth
            )
        }

        if (editingBudget != null) {
            viewModel.updateBudget(budget)
        } else {
            viewModel.addBudget(budget)
        }

        Toast.makeText(context, "Budget saved successfully!", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
