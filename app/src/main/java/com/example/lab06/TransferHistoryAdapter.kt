package com.example.lab06

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransferHistoryAdapter(
    private val transfers: List<TransferRequest>
) : RecyclerView.Adapter<TransferHistoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val row = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transfer_history, parent, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(transfers[position])
    }

    override fun getItemCount(): Int = transfers.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.tvHistoryName)
        private val amount: TextView = view.findViewById(R.id.tvHistoryAmount)
        private val remarks: TextView = view.findViewById(R.id.tvHistoryRemarks)

        fun bind(transfer: TransferRequest) {
            val context = itemView.context
            name.text = context.getString(
                R.string.recipient_details, transfer.recipientName, transfer.recipientAccount
            )
            amount.text = context.getString(R.string.lkr_amount, transfer.amount)
            remarks.text = transfer.remarks.ifEmpty { context.getString(R.string.no_remarks) }
        }
    }
}
