package com.example.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

object ImageCaptureHelper {

    fun createCameraCaptureUri(context: Context, prefix: String): Uri {
        val dir = File(context.cacheDir, "camera_captures").apply { mkdirs() }
        val file = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    suspend fun decodeSampledBitmap(
        context: Context,
        uri: Uri,
        maxDimension: Int = 1024
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            val width = boundsOptions.outWidth
            val height = boundsOptions.outHeight
            if (width <= 0 || height <= 0) return@withContext null

            var sampleSize = 1
            while (max(width / sampleSize, height / sampleSize) > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        } catch (_: Exception) {
            null
        }
    }
}
