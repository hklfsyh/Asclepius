package com.dicoding.asclepius.view

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.data.AppDatabase
import com.dicoding.asclepius.databinding.ActivityHistoryBinding
import com.dicoding.asclepius.model.PredictionHistory
import com.dicoding.asclepius.view.adapter.HistoryAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(this)

        historyAdapter = HistoryAdapter { prediction ->
            deletePrediction(prediction)
        }

        binding.recyclerViewHistory.adapter = historyAdapter

        fetchPredictions()
    }

    private fun fetchPredictions() {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(applicationContext)
            val predictions = database.predictionHistoryDao().getAllPredictions()

            Log.d("HistoryActivity", "Fetched Predictions: ${predictions.size} predictions found")

            withContext(Dispatchers.Main) {
                historyAdapter.submitList(predictions)
            }
        }
    }

    private fun deletePrediction(prediction: PredictionHistory) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(applicationContext)
            database.predictionHistoryDao().deletePrediction(prediction)

            withContext(Dispatchers.Main) {
                fetchPredictions()
                Log.d("HistoryActivity", "Prediction deleted: ${prediction.result}")
            }
        }
    }
}
