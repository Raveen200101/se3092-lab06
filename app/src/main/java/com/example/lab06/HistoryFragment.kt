package com.example.lab06

import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class HistoryFragment : Fragment(R.layout.fragment_history) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val list = view.findViewById<RecyclerView>(R.id.rvHistory)
        val status = view.findViewById<TextView>(R.id.tvHistoryStatus)
        list.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            val transfers = try {
                AppDatabase.getInstance(requireContext()).transferDao().getAll()
            } catch (_: SQLiteException) {
                status.setText(R.string.history_error)
                return@launch
            }
            list.adapter = TransferHistoryAdapter(transfers)
            status.visibility = if (transfers.isEmpty()) View.VISIBLE else View.GONE
            status.setText(R.string.empty_history)
        }
    }
}
