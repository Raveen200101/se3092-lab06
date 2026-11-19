package com.example.lab06

import android.content.Context
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.edit
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfirmationFragment : Fragment(R.layout.fragment_confirmation) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val request = requireNotNull(
            BundleCompat.getSerializable(
                requireArguments(), ARG_TRANSFER_REQUEST, TransferRequest::class.java
            )
        )
        view.findViewById<TextView>(R.id.tvConfirmRecipient).text =
            getString(R.string.recipient_details, request.recipientName, request.recipientAccount)
        view.findViewById<TextView>(R.id.tvConfirmAmount).text =
            getString(R.string.lkr_amount, request.amount)
        view.findViewById<TextView>(R.id.tvConfirmRemarks).text =
            request.remarks.ifEmpty { getString(R.string.no_remarks) }

        val confirm = view.findViewById<Button>(R.id.btnConfirm)
        val edit = view.findViewById<Button>(R.id.btnEditTransfer)
        val error = view.findViewById<TextView>(R.id.tvSaveError)
        val saveModel = ViewModelProvider(this)[TransferSaveModel::class.java]
        val blockBackWhileSaving = requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, enabled = false
        ) {}

        edit.setOnClickListener { parentFragmentManager.popBackStack() }
        confirm.setOnClickListener {
            saveModel.save(requireContext().applicationContext, request)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                saveModel.status.collect { status ->
                    val canEdit = status == SaveStatus.READY || status == SaveStatus.FAILED
                    confirm.isEnabled = canEdit
                    edit.isEnabled = canEdit
                    blockBackWhileSaving.isEnabled = !canEdit
                    error.visibility = if (status == SaveStatus.FAILED) View.VISIBLE else View.GONE
                    if (status == SaveStatus.SAVED) {
                        Toast.makeText(requireContext(), R.string.transfer_submitted, Toast.LENGTH_SHORT).show()
                        (requireActivity() as MainActivity).returnToDashboard()
                    }
                }
            }
        }
    }

    companion object {
        const val ARG_TRANSFER_REQUEST = "transfer_request"
    }
}

internal enum class SaveStatus { READY, SAVING, SAVED, FAILED }

internal class TransferSaveModel : ViewModel() {
    private val _status = MutableStateFlow(SaveStatus.READY)
    val status = _status.asStateFlow()

    fun save(context: Context, request: TransferRequest) {
        if (_status.value == SaveStatus.SAVING || _status.value == SaveStatus.SAVED) return

        _status.value = SaveStatus.SAVING
        viewModelScope.launch {
            _status.value = try {
                AppDatabase.getInstance(context).transferDao().insert(request)
                context.lastRecipientPreferences().edit {
                    putString(LAST_RECIPIENT_ACCOUNT, request.recipientAccount)
                    putString(LAST_RECIPIENT_NAME, request.recipientName)
                }
                SaveStatus.SAVED
            } catch (_: SQLiteException) {
                SaveStatus.FAILED
            }
        }
    }
}
