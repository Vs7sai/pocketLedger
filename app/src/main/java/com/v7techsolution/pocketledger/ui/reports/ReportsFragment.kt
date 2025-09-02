package com.v7techsolution.pocketledger.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.button.MaterialButton
import com.v7techsolution.pocketledger.databinding.FragmentReportsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        // Observe monthly summary
        viewModel.monthlySummary.observe(viewLifecycleOwner, Observer { summary ->
            updateMonthlySummaryDisplay(summary)
        })

        // Observe category breakdown
        viewModel.categoryBreakdown.observe(viewLifecycleOwner, Observer { breakdown ->
            updateCategoryBreakdownDisplay(breakdown)
        })

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    private fun setupClickListeners() {
        binding.buttonCurrentMonth.setOnClickListener {
            viewModel.setCurrentMonth()
        }

        binding.buttonPreviousMonth.setOnClickListener {
            viewModel.setPreviousMonth()
        }

        binding.buttonNextMonth.setOnClickListener {
            viewModel.setNextMonth()
        }

        binding.buttonExport.setOnClickListener {
            exportReport()
        }
    }

    private fun updateMonthlySummaryDisplay(summary: MonthlySummary) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        
        binding.textViewMonth.text = summary.month.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"))
        binding.textViewTotalIncome.text = formatter.format(summary.totalIncome)
        binding.textViewTotalExpenses.text = formatter.format(summary.totalExpenses)
        binding.textViewNetAmount.text = formatter.format(summary.netAmount)
        
        // Set colors based on net amount
        val colorRes = when {
            summary.netAmount > BigDecimal.ZERO -> com.v7techsolution.pocketledger.R.color.positive_balance
            summary.netAmount < BigDecimal.ZERO -> com.v7techsolution.pocketledger.R.color.negative_balance
            else -> com.v7techsolution.pocketledger.R.color.neutral_balance
        }
        binding.textViewNetAmount.setTextColor(resources.getColor(colorRes, null))
    }

    private fun updateCategoryBreakdownDisplay(breakdown: List<CategorySpending>) {
        // Update category breakdown chart
        // This would typically involve a chart library like MPAndroidChart
        // For now, we'll show a simple list
        binding.textViewCategoryBreakdown.text = breakdown.joinToString("\n") { category ->
            "${category.categoryName}: ${NumberFormat.getCurrencyInstance(Locale.getDefault()).format(category.amount)}"
        }
    }

    private fun exportReport() {
        viewModel.exportReport()
        // Show success message
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "Report exported successfully!",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
