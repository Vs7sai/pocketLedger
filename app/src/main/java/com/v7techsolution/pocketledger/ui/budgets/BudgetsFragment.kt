package com.v7techsolution.pocketledger.ui.budgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.v7techsolution.pocketledger.data.entity.Budget
import com.v7techsolution.pocketledger.databinding.FragmentBudgetsBinding
import com.v7techsolution.pocketledger.ui.budgets.BudgetWithCategory
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.YearMonth
import java.util.*

@AndroidEntryPoint
class BudgetsFragment : Fragment() {

    private var _binding: FragmentBudgetsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetsViewModel by viewModels()
    private lateinit var budgetAdapter: BudgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        budgetAdapter = BudgetAdapter(
            budgets = emptyList(),
            onBudgetClick = { budget -> showEditBudgetDialog(budget) }
        )
        binding.recyclerViewBudgets.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = budgetAdapter
        }
    }

    private fun setupObservers() {
        // Observe budgets and categories together
        viewModel.budgets.observe(viewLifecycleOwner, Observer { budgets ->
            viewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
                if (budgets.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.recyclerViewBudgets.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.recyclerViewBudgets.visibility = View.VISIBLE
                    
                    // Create BudgetWithCategory objects
                    val budgetsWithCategories = budgets.mapNotNull { budget ->
                        val category = categories.find { it.id == budget.categoryId }
                        if (category != null) {
                            BudgetWithCategory(budget, category)
                        } else null
                    }
                    
                    budgetAdapter = BudgetAdapter(budgetsWithCategories) { budget -> showEditBudgetDialog(budget) }
                    binding.recyclerViewBudgets.adapter = budgetAdapter
                }
            })
        })

        // Observe total budgeted amount
        viewModel.totalBudgeted.observe(viewLifecycleOwner, Observer { total ->
            updateTotalBudgetedDisplay(total)
        })

        // Observe loading state - removed progress bar for cleaner UI
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            // Loading state handled by individual components
        })
    }

    private fun setupClickListeners() {
        binding.fabAddBudget.setOnClickListener {
            showAddBudgetDialog()
        }

        binding.buttonCurrentMonth.setOnClickListener {
            viewModel.setCurrentMonth()
        }

        binding.buttonAddCategoryBudget.setOnClickListener {
            showAddBudgetDialog()
        }

        binding.buttonAddOverallBudget.setOnClickListener {
            showAddOverallBudgetDialog()
        }
    }

    private fun updateTotalBudgetedDisplay(total: BigDecimal) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val formattedTotal = formatter.format(total)
        binding.textViewTotalBudgeted.text = formattedTotal
    }

    private fun showAddBudgetDialog() {
        AddEditBudgetDialog().show(
            childFragmentManager,
            "AddBudget"
        )
    }

    private fun showAddOverallBudgetDialog() {
        // For now, show the same dialog
        AddEditBudgetDialog().show(
            childFragmentManager,
            "AddOverallBudget"
        )
    }

    private fun showEditBudgetDialog(budget: Budget) {
        AddEditBudgetDialog.newInstance(budget).show(
            childFragmentManager,
            "EditBudget"
        )
    }

    private fun showDeleteBudgetDialog(budget: Budget) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Budget")
            .setMessage("Are you sure you want to delete this budget? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteBudget(budget)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
