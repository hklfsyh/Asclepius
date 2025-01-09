package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class ImageClassifierHelper(
    private val context: Context,
    private val imageClassifierListener: ClassifierListener?
) {
    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        try {
            val baseOptions = BaseOptions.builder()
                .setNumThreads(2)
                .build()
            val options = ImageClassifier.ImageClassifierOptions.builder()
                .setBaseOptions(baseOptions)
                .setMaxResults(1)
                .setScoreThreshold(0.5f)
                .build()

            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                "cancer_classification.tflite",
                options
            )
        } catch (e: IllegalStateException) {
            imageClassifierListener?.onError("Image classifier failed to initialize. See error logs for details")
            return
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }.copy(Bitmap.Config.ARGB_8888, true)

            bitmap?.let { processImage(it) }
        } catch (e: Exception) {
            imageClassifierListener?.onError("Failed to load image: ${e.message}")
        }
    }

    private fun processImage(bitmap: Bitmap) {
        try {
            val imageProcessor = ImageProcessor.Builder().build()
            val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

            val results = imageClassifier?.classify(tensorImage)
            results?.let { classifications ->
                if (classifications.isNotEmpty() && classifications[0].categories.isNotEmpty()) {
                    imageClassifierListener?.onResults(
                        classifications[0].categories[0].label,
                        classifications[0].categories[0].score
                    )
                } else {
                    imageClassifierListener?.onError("No results found")
                }
            }
        } catch (e: Exception) {
            imageClassifierListener?.onError("Failed to process image: ${e.message}")
        }
    }

    interface ClassifierListener {
        fun onResults(label: String, confidence: Float)
        fun onError(error: String)
    }

}