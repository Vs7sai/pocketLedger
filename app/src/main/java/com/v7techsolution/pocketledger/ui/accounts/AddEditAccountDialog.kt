package com.v7techsolution.pocketledger.ui.accounts

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
import com.v7techsolution.pocketledger.R
import com.v7techsolution.pocketledger.data.entity.Account
import com.v7techsolution.pocketledger.data.entity.AccountType
import com.v7techsolution.pocketledger.databinding.DialogAddEditAccountBinding
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@AndroidEntryPoint
class AddEditAccountDialog : DialogFragment() {

    private var _binding: DialogAddEditAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountsViewModel by viewModels()
    private var editingAccount: Account? = null

    companion object {
        fun newInstance(account: Account): AddEditAccountDialog {
            return AddEditAccountDialog().apply {
                editingAccount = account
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddEditAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupClickListeners()
        populateFieldsIfEditing()
    }

    private fun setupUI() {
        // Setup account type dropdown
        val accountTypes = AccountType.values().map { it.name.replace("_", " ") }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, accountTypes)
        binding.spinnerAccountType.setAdapter(adapter)

        // Set title based on mode
        binding.textViewTitle.text = if (editingAccount == null) "Add Account" else "Edit Account"
    }

    private fun setupClickListeners() {
        binding.buttonSave.setOnClickListener {
            saveAccount()
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun populateFieldsIfEditing() {
        editingAccount?.let { account ->
            binding.editTextAccountName.setText(account.name)
            binding.editTextBalance.setText(account.balance.toString())
            binding.editTextIcon.setText(account.icon)
            
            // Set account type
            val accountTypeText = account.type.name.replace("_", " ")
            binding.spinnerAccountType.setText(accountTypeText, false)
        }
    }

    private fun saveAccount() {
        val name = binding.editTextAccountName.text.toString().trim()
        val balanceText = binding.editTextBalance.text.toString().trim()
        val icon = binding.editTextIcon.text.toString().trim()
        val selectedTypeText = binding.spinnerAccountType.text.toString()
        val accountType = AccountType.values().find { it.name.replace("_", " ") == selectedTypeText }
            ?: AccountType.CASH

        // Validation
        if (name.isEmpty()) {
            binding.editTextAccountName.error = "Account name is required"
            return
        }

        if (balanceText.isEmpty()) {
            binding.editTextBalance.error = "Balance is required"
            return
        }

        val balance = try {
            BigDecimal(balanceText).setScale(2, RoundingMode.HALF_UP)
        } catch (e: NumberFormatException) {
            binding.editTextBalance.error = "Invalid balance amount"
            return
        }

        if (icon.isEmpty()) {
            binding.editTextIcon.error = "Icon is required"
            return
        }

        val account = if (editingAccount != null) {
            editingAccount!!.copy(
                name = name,
                type = accountType,
                balance = balance,
                icon = icon,
                updatedAt = LocalDateTime.now()
            )
        } else {
            Account(
                name = name,
                type = accountType,
                balance = balance,
                color = getRandomColor(),
                icon = icon
            )
        }

        if (editingAccount != null) {
            viewModel.updateAccount(account)
        } else {
            viewModel.addAccount(account)
        }

        Toast.makeText(context, "Account saved successfully!", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    private fun getRandomColor(): Int {
        val colors = listOf(
            0xFFE57373.toInt(), // Red
            0xFF81C784.toInt(), // Green
            0xFF64B5F6.toInt(), // Blue
            0xFFFFB74D.toInt(), // Orange
            0xFFBA68C8.toInt(), // Purple
            0xFF4DB6AC.toInt(), // Teal
            0xFFFF8A65.toInt(), // Deep Orange
            0xFF90A4AE.toInt()  // Blue Grey
        )
        return colors.random()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
