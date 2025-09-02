package com.v7techsolution.pocketledger.ui.analytics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.v7techsolution.pocketledger.databinding.FragmentAnalyticsChartsBinding
import com.v7techsolution.pocketledger.ui.reports.ReportsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class AnalyticsChartsFragment : Fragment() {

    private var _binding: FragmentAnalyticsChartsBinding? = null
    private val binding get() = _binding!!

    private val reportsViewModel: ReportsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupBarChart()
        reportsViewModel.refreshData()
    }

    private fun setupObservers() {
        reportsViewModel.monthlySummary.observe(viewLifecycleOwner, Observer { summary ->
            val currency = NumberFormat.getCurrencyInstance(Locale.getDefault())
            // We have text labels inside the cards; nothing to bind directly but could be extended later
            // This ensures data is loaded and charts get fed via the breakdown observer
        })

        reportsViewModel.categoryBreakdown.observe(viewLifecycleOwner, Observer { breakdown ->
            val entries = breakdown.take(8).mapIndexed { index, item ->
                BarEntry(index.toFloat(), item.amount.toFloat())
            }
            val labels = breakdown.take(8).map { it.categoryName }

            val dataSet = BarDataSet(entries, "Expenses by Category").apply {
                color = Color.parseColor("#3B82F6")
                valueTextColor = Color.parseColor("#1E293B")
                valueTextSize = 12f
            }
            val data = BarData(dataSet)
            binding.barChart.apply {
                this.data = data
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                axisLeft.textColor = Color.parseColor("#64748B")
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = Color.parseColor("#64748B")
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val i = value.toInt()
                        return if (i in labels.indices) labels[i] else ""
                    }
                }
                animateY(800)
                invalidate()
            }
        })
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            setNoDataText("No data yet")
            setNoDataTextColor(Color.parseColor("#94A3B8"))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


