package com.example.lab06

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.lab06.databinding.FragmentTransferBinding

class TransferFragment : Fragment() {
    private var _binding: FragmentTransferBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.btnSubmit.setOnClickListener { onSubmitTransfer() }
        binding.btnCancel.setOnClickListener { parentFragmentManager.popBackStack() }

        val prefs = requireContext().lastRecipientPreferences()
        binding.etRecipientAccount.setText(prefs.getString(LAST_RECIPIENT_ACCOUNT, ""))
        binding.etRecipientName.setText(prefs.getString(LAST_RECIPIENT_NAME, ""))
    }

    override fun onDestroyView() {
        _binding?.unbind()
        _binding = null
        super.onDestroyView()
    }

    private fun onSubmitTransfer() {
        val account = binding.etRecipientAccount.text.toString().trim()
        val name = binding.etRecipientName.text.toString().trim()
        val amount = binding.etAmount.text.toString().trim().toDoubleOrNull()
        val remarks = binding.etRemarks.text.toString().trim()

        if (account.isEmpty()) {
            binding.etRecipientAccount.apply {
                requestFocus()
                error = getString(R.string.account_required)
            }
            return
        }
        if (name.isEmpty()) {
            binding.etRecipientName.apply {
                requestFocus()
                error = getString(R.string.name_required)
            }
            return
        }
        if (amount == null || !amount.isFinite() || amount <= 0.0) {
            binding.etAmount.apply {
                requestFocus()
                error = getString(R.string.amount_invalid)
            }
            return
        }

        val host = requireActivity() as MainActivity
        WindowCompat.getInsetsController(host.window, binding.root).hide(WindowInsetsCompat.Type.ime())
        binding.btnSubmit.isEnabled = false
        binding.btnCancel.isEnabled = false
        host.showConfirmationFragment(TransferRequest(account, name, amount, remarks))
    }
}
