package com.v7techsolution.pocketledger.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.v7techsolution.pocketledger.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        // Observe settings changes
        viewModel.isAnalyticsEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            binding.switchAnalytics.isChecked = enabled
        })

        viewModel.isNotificationsEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            binding.switchNotifications.isChecked = enabled
        })
    }

    private fun setupClickListeners() {
        // Privacy section
        binding.cardPrivacy.setOnClickListener {
            showPrivacyInfo()
        }

        // Export section
        binding.cardExport.setOnClickListener {
            exportData()
        }

        // Import section
        binding.cardImport.setOnClickListener {
            importData()
        }

        // CSV section
        binding.cardCsv.setOnClickListener {
            showCsvOptions()
        }

        // Analytics toggle
        binding.switchAnalytics.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAnalyticsEnabled(isChecked)
        }

        // Notifications toggle
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificationsEnabled(isChecked)
        }

        // About section
        binding.cardAbout.setOnClickListener {
            showAboutDialog()
        }

        // Help section
        binding.cardHelp.setOnClickListener {
            showHelpDialog()
        }
    }

    private fun showPrivacyInfo() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Privacy & Data")
            .setMessage("Your data stays on this device. No cloud storage, no tracking, no sharing with third parties. Your financial information is encrypted and stored locally.")
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun exportData() {
        viewModel.exportData()
        Snackbar.make(
            binding.root,
            "Export started...",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun importData() {
        viewModel.importData()
        Snackbar.make(
            binding.root,
            "Import started...",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showCsvOptions() {
        val options = arrayOf("Import CSV", "Export CSV", "CSV Mapping")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("CSV Operations")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.importCsv()
                    1 -> viewModel.exportCsv()
                    2 -> viewModel.showCsvMapping()
                }
            }
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("About PocketLedger")
            .setMessage("PocketLedger v1.0.0\n\nA secure, private personal finance manager that keeps your data on your device.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showHelpDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Help & Support")
            .setMessage("Need help? Check out our documentation or contact support.")
            .setPositiveButton("Documentation") { _, _ ->
                // Open documentation
            }
            .setNegativeButton("Contact Support") { _, _ ->
                // Contact support
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
