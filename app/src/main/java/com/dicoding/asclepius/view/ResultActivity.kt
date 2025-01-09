package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.dicoding.asclepius.data.AppDatabase
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.model.PredictionHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.util.Locale

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentImageUri = intent.getParcelableExtra(EXTRA_IMAGE_URI)
        val label = intent.getStringExtra(EXTRA_RESULT_LABEL) ?: "Unknown"
        val confidence = intent.getFloatExtra(EXTRA_RESULT_CONFIDENCE, 0f)

        binding.saveHistory.setOnClickListener {
            savePredictionHistory(currentImageUri?.toString(), label, confidence)
        }

        currentImageUri?.let {
            binding.resultImage.setImageURI(it)
        }

        val confidencePercent = NumberFormat.getPercentInstance(Locale.US).apply {
            maximumFractionDigits = 1
        }.format(confidence)

        val resultText = """
            Detected $label
            Confidence Score: $confidencePercent
        """.trimIndent()

        binding.resultText.text = resultText
    }

    private fun savePredictionHistory(imageUri: String?, label: String, confidence: Float) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(applicationContext)
            val predictionHistory = PredictionHistory(
                imageUri = imageUri ?: "",
                result = label,
                confidenceScore = confidence
            )
            database.predictionHistoryDao().insertPrediction(predictionHistory)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@ResultActivity,
                    "Prediction saved to history",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        startActivity(Intent(this, HistoryActivity::class.java))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_IMAGE_URI, currentImageUri)
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT_LABEL = "extra_result_label"
        const val EXTRA_RESULT_CONFIDENCE = "extra_result_confidence"
    }
}