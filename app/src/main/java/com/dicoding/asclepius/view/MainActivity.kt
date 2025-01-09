package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID

class MainActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {
    private lateinit var binding: ActivityMainBinding
    private var currentImageUri: Uri? = null
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageClassifierHelper = ImageClassifierHelper(this, this)

        setButtonAnimations()

        binding.analyzeButton.setOnClickListener {
            analyzeImage()
        }
        binding.galleryButton.setOnClickListener {
            startGallery()
        }
        binding.clearButton.setOnClickListener {
            clearImage()
        }
        binding.viewHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
        binding.viewNews.setOnClickListener {
            val intent = Intent(this, NewsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_${UUID.randomUUID()}.jpg"))

        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(16f, 9f) // 1:1 aspect ratio
            .withMaxResultSize(1080, 1080) // Max resolution
            .withOptions(UCrop.Options().apply {
                setCompressionQuality(80) // Image quality
                setHideBottomControls(false)
                setFreeStyleCropEnabled(true)
                setToolbarTitle("Crop Image")
            })

        try {
            uCrop.start(this)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error starting crop: ${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            currentImageUri = resultUri
            showImage()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            showToast("Error cropping image: ${cropError?.message}")
        }
    }

    private fun startGallery() {
        launcherGallery.launch("image/*")
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            startCrop(uri)
        } else {
            showToast("No image selected")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
            binding.analyzeButton.isEnabled = true

            val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            binding.previewImageView.startAnimation(fadeIn)
        }

        binding.clearButton.visibility = View.VISIBLE
    }

    private fun analyzeImage() {
        currentImageUri?.let {
            showLoading(true)
            imageClassifierHelper.classifyStaticImage(it)
        } ?: showToast("Please select an image first")
    }

    private fun moveToResult(label: String, confidence: Float) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri)
            putExtra(ResultActivity.EXTRA_RESULT_LABEL, label)
            putExtra(ResultActivity.EXTRA_RESULT_CONFIDENCE, confidence)
        }
        startActivity(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.galleryButton.isEnabled = !isLoading
        binding.analyzeButton.isEnabled = !isLoading
    }

    private fun clearImage() {
        currentImageUri = null
        binding.previewImageView.setImageURI(null)
        binding.clearButton.visibility = View.GONE
        showToast("Picture deleted, please choose a new one.")
    }

    private fun setButtonAnimations() {
        val scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        val scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down)

        binding.galleryButton.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> v.startAnimation(scaleUp)
                android.view.MotionEvent.ACTION_UP -> v.startAnimation(scaleDown)
            }
            false
        }

        binding.analyzeButton.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> v.startAnimation(scaleUp)
                android.view.MotionEvent.ACTION_UP -> v.startAnimation(scaleDown)
            }
            false
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResults(label: String, confidence: Float) {
        showLoading(false)
        moveToResult(label, confidence)
    }

    override fun onError(error: String) {
        showLoading(false)
        showToast(error)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_IMAGE_URI, currentImageUri)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentImageUri = savedInstanceState.getParcelable(EXTRA_IMAGE_URI)
        showImage()
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }
}
