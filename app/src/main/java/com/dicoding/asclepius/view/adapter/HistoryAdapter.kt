package com.dicoding.asclepius.view.adapter

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ItemHistoryBinding
import com.dicoding.asclepius.model.PredictionHistory
import java.util.*

class HistoryAdapter(
    private val deleteCallback: (PredictionHistory) -> Unit
) : ListAdapter<PredictionHistory, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val prediction = getItem(position)
        holder.bind(prediction)
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding, private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(prediction: PredictionHistory) {
            binding.textViewResult.text = prediction.result
            binding.textViewConfidence.text = "Confidence: ${prediction.confidenceScore * 100}%"

            Log.d("HistoryAdapter", "Loading image from URI: ${prediction.imageUri}")

            Glide.with(binding.root.context).load(prediction.imageUri)
                .placeholder(R.drawable.ic_place_holder)
                .into(binding.imageViewResult)

            val formattedDate = formatTimestamp(prediction.timestamp)
            binding.textViewTimestamp.text = formattedDate

            binding.buttonDelete.setOnClickListener {
                showDeleteConfirmation(prediction)
            }
        }

        private fun showDeleteConfirmation(prediction: PredictionHistory) {
            AlertDialog.Builder(context).apply {
                setTitle("Delete History")
                setMessage("Are you sure you want to delete this history?")
                setPositiveButton("Delete") { _, _ ->
                    deleteCallback(prediction)
                    Toast.makeText(context, "History deleted", Toast.LENGTH_SHORT).show()
                }
                setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                create()
                show()
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            return DateFormat.format("dd MMM yyyy, HH:mm", calendar).toString()
        }
    }

    class HistoryDiffCallback : DiffUtil.ItemCallback<PredictionHistory>() {
        override fun areItemsTheSame(
            oldItem: PredictionHistory, newItem: PredictionHistory
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PredictionHistory, newItem: PredictionHistory
        ): Boolean {
            return oldItem == newItem
        }
    }
}
