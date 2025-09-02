package com.v7techsolution.pocketledger.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.v7techsolution.pocketledger.R
import com.v7techsolution.pocketledger.data.entity.Account
import com.v7techsolution.pocketledger.databinding.FragmentAccountsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class AccountsFragment : Fragment() {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountsViewModel by viewModels()
    private lateinit var accountAdapter: AccountAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        accountAdapter = AccountAdapter(
            onEditClick = { account -> showEditAccountDialog(account) },
            onDeleteClick = { account -> showDeleteAccountDialog(account) }
        )
        binding.recyclerViewAccounts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = accountAdapter
        }
    }

    private fun setupObservers() {
        // Observe accounts
        viewModel.accounts.observe(viewLifecycleOwner, Observer { accounts ->
            if (accounts.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerViewAccounts.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.recyclerViewAccounts.visibility = View.VISIBLE
                accountAdapter.submitList(accounts)
            }
        })

        // Observe total net worth
        viewModel.totalNetWorth.observe(viewLifecycleOwner, Observer { netWorth ->
            updateNetWorthDisplay(netWorth)
        })

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    private fun setupClickListeners() {
        binding.fabAddAccount.setOnClickListener {
            showAddAccountDialog()
        }
    }

    private fun updateNetWorthDisplay(netWorth: BigDecimal) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val formattedNetWorth = formatter.format(netWorth)
        binding.textViewNetWorth.text = formattedNetWorth
        
        // Set color based on net worth
        val colorRes = when {
            netWorth > BigDecimal.ZERO -> R.color.positive_balance
            netWorth < BigDecimal.ZERO -> R.color.negative_balance
            else -> R.color.neutral_balance
        }
        binding.textViewNetWorth.setTextColor(resources.getColor(colorRes, null))
    }

    private fun showAddAccountDialog() {
        AddEditAccountDialog().show(
            childFragmentManager,
            "AddAccount"
        )
    }

    private fun showEditAccountDialog(account: Account) {
        AddEditAccountDialog.newInstance(account).show(
            childFragmentManager,
            "EditAccount"
        )
    }

    private fun showDeleteAccountDialog(account: Account) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete '${account.name}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAccount(account)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
