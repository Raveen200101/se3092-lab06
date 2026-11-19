package com.example.lab06

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import kotlinx.coroutines.launch

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private var requestedNotificationPermission = false
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startSessionReminder()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedNotificationPermission = savedInstanceState?.getBoolean(PERMISSION_REQUESTED) ?: false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(PERMISSION_REQUESTED, requestedNotificationPermission)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val host = requireActivity() as MainActivity
        view.findViewById<Button>(R.id.btnGoToTransfer).setOnClickListener {
            it.isEnabled = false
            view.findViewById<Button>(R.id.btnGoToHistory).isEnabled = false
            host.showTransferFragment()
        }
        view.findViewById<Button>(R.id.btnGoToHistory).setOnClickListener {
            it.isEnabled = false
            view.findViewById<Button>(R.id.btnGoToTransfer).isEnabled = false
            host.showHistoryFragment()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.withResumed {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(host, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    startSessionReminder()
                } else if (!requestedNotificationPermission) {
                    requestedNotificationPermission = true
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun startSessionReminder() {
        val context = requireContext()
        context.startService(Intent(context, SessionReminderService::class.java))
    }

    private companion object {
        const val PERMISSION_REQUESTED = "notification_permission_requested"
    }
}
