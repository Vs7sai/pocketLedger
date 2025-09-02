package com.v7techsolution.pocketledger.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.v7techsolution.pocketledger.databinding.FragmentHomeBinding
import com.v7techsolution.pocketledger.ui.transaction.QuickAddTransactionDialog
import com.v7techsolution.pocketledger.data.database.SQLiteDatabaseHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionHomeAdapter
    
    @Inject
    lateinit var dbHelper: SQLiteDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupCharts()
        
        // Check database state for debugging
        checkDatabaseState()
        
        viewModel.loadTodayData()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionHomeAdapter()
        binding.recyclerViewTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }
    }

    private fun setupObservers() {
        // Observe today's balance
        viewModel.todayBalance.observe(viewLifecycleOwner, Observer { balance ->
            updateBalanceDisplay(balance)
        })

        // Observe monthly income
        viewModel.monthlyIncome.observe(viewLifecycleOwner, Observer { income ->
            updateMonthlyIncomeDisplay(income)
        })

        // Observe monthly expenses
        viewModel.monthlyExpenses.observe(viewLifecycleOwner, Observer { expenses ->
            updateMonthlyExpensesDisplay(expenses)
        })

        // Observe net income
        viewModel.netIncome.observe(viewLifecycleOwner, Observer { netIncome ->
            updateNetIncomeDisplay(netIncome)
        })

        // Observe total transactions
        viewModel.totalTransactions.observe(viewLifecycleOwner, Observer { total ->
            updateTotalTransactionsDisplay(total)
        })

        // Observe recent transactions
        viewModel.recentTransactions.observe(viewLifecycleOwner, Observer { transactions ->
            if (transactions.isEmpty()) {
                // Show empty state message in the recycler view itself
                binding.recyclerViewTransactions.visibility = View.GONE
            } else {
                binding.recyclerViewTransactions.visibility = View.VISIBLE
                transactionAdapter.submitList(transactions)
            }
        })

        // Observe loading state - removed progress bar for cleaner UI
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            // Loading state handled by individual components
        })
    }

    private fun setupClickListeners() {
        // Quick Add Transaction
        binding.fabAddTransaction.setOnClickListener {
            showQuickAddDialog()
        }

        // Navigation Buttons
        binding.btnBillsNotifier.setOnClickListener {
            showBillsNotifierInfo()
        }

        binding.btnAnalytics.setOnClickListener {
            // Navigate to Analytics screen via NavController
            try {
                findNavController().navigate(com.v7techsolution.pocketledger.R.id.navigation_analytics)
            } catch (e: Exception) {
                showAnalyticsInfo()
            }
        }

        binding.btnTransaction.setOnClickListener {
            showQuickAddDialog()
        }

        binding.btnBudgets.setOnClickListener {
            // Navigate to Budgets screen
            // For now, show info dialog
            showBudgetsInfo()
        }
    }

    private fun updateBalanceDisplay(balance: BigDecimal) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val formattedBalance = formatter.format(balance)
        
        // Update the net income display with the balance
        binding.textViewNetIncome.text = formattedBalance
        
        // Update color based on balance
        val colorRes = when {
            balance > BigDecimal.ZERO -> com.v7techsolution.pocketledger.R.color.positive_balance
            balance < BigDecimal.ZERO -> com.v7techsolution.pocketledger.R.color.negative_balance
            else -> com.v7techsolution.pocketledger.R.color.text_secondary
        }
        binding.textViewNetIncome.setTextColor(resources.getColor(colorRes, null))
    }

    private fun updateMonthlyIncomeDisplay(income: BigDecimal) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        binding.textViewMonthlyIncome.text = formatter.format(income)
    }

    private fun updateMonthlyExpensesDisplay(expenses: BigDecimal) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        binding.textViewMonthlyExpenses.text = formatter.format(expenses)
    }

    private fun updateNetIncomeDisplay(netIncome: BigDecimal) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        binding.textViewNetIncome.text = formatter.format(netIncome)
        
        // Update color based on net income
        val colorRes = when {
            netIncome > BigDecimal.ZERO -> com.v7techsolution.pocketledger.R.color.positive_balance
            netIncome < BigDecimal.ZERO -> com.v7techsolution.pocketledger.R.color.negative_balance
            else -> com.v7techsolution.pocketledger.R.color.text_secondary
        }
        binding.textViewNetIncome.setTextColor(resources.getColor(colorRes, null))
    }

    private fun updateTotalTransactionsDisplay(total: Int) {
        binding.textViewTotalTransactions.text = total.toString()
    }

    private fun showQuickAddDialog() {
        val dialog = QuickAddTransactionDialog()
        dialog.setTransactionSavedCallback(object : QuickAddTransactionDialog.TransactionSavedCallback {
            override fun onTransactionSaved() {
                refreshDataAfterTransaction()
            }
        })
        dialog.show(
            childFragmentManager,
            "QuickAddTransaction"
        )
    }

    private fun showBillsNotifierInfo() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Bills Notifier")
            .setMessage("Set up recurring bills and get notified before they're due. You'll receive morning and evening reminders based on your preferences.")
            .setPositiveButton("Got it") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showAnalyticsInfo() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Analytics & Charts")
            .setMessage("View detailed financial analytics, spending trends, and interactive charts to understand your financial patterns.")
            .setPositiveButton("Got it") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showBudgetsInfo() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Budget Management")
            .setMessage("Set spending limits, track your progress, and manage your budgets by category to stay on top of your finances.")
            .setPositiveButton("Got it") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupCharts() {
        setupPieChart()
    }

    private fun setupPieChart() {
        val pieChart = binding.pieChart
        
        // Sample data for the pie chart
        val entries = listOf(
            PieEntry(65f, "Expenses"),
            PieEntry(35f, "Income")
        )
        
        val dataSet = PieDataSet(entries, "Income vs Expenses")
        dataSet.colors = listOf(
            resources.getColor(com.v7techsolution.pocketledger.R.color.negative_balance, null),
            resources.getColor(com.v7techsolution.pocketledger.R.color.positive_balance, null)
        )
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = resources.getColor(com.v7techsolution.pocketledger.R.color.text_primary, null)
        
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(false)
        pieChart.setDrawHoleEnabled(true)
        pieChart.holeRadius = 58f
        pieChart.setHoleColor(resources.getColor(com.v7techsolution.pocketledger.R.color.white, null))
        pieChart.setTransparentCircleRadius(61f)
        pieChart.setTransparentCircleColor(resources.getColor(com.v7techsolution.pocketledger.R.color.white, null))
        pieChart.setTransparentCircleAlpha(110)
        pieChart.centerText = "65%\nExpenses"
        pieChart.setCenterTextSize(16f)
        pieChart.setCenterTextColor(resources.getColor(com.v7techsolution.pocketledger.R.color.text_primary, null))
        pieChart.setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD)
        pieChart.animateY(1000)
    }
    
    private fun checkDatabaseState() {
        try {
            val isHealthy = dbHelper.checkDatabaseState()
            android.util.Log.d("HomeFragment", "Database state check result: $isHealthy")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error checking database state", e)
        }
    }
    
    fun refreshDataAfterTransaction() {
        android.util.Log.d("HomeFragment", "Refreshing data after new transaction")
        viewModel.refreshData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
