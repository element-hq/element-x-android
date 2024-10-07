/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import io.element.android.libraries.androidutils.bitmap.calculateInSampleSize
import io.element.android.libraries.androidutils.bitmap.resizeToMax
import io.element.android.libraries.androidutils.bitmap.rotateToMetadataOrientation
import io.element.android.libraries.androidutils.file.createTmpFile
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: CoroutineDispatchers,
) {
    /**
     * Decodes the [inputStream] into a [Bitmap] and applies the needed transformations (rotation, scale) based on [resizeMode], then writes it into a
     * temporary file using the passed [format], [orientation] and [desiredQuality].
     * @return a [Result] containing the resulting [ImageCompressionResult] with the temporary [File] and some metadata.
     */
    suspend fun compressToTmpFile(
        inputStreamProvider: () -> InputStream,
        resizeMode: ResizeMode,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        orientation: Int = ExifInterface.ORIENTATION_UNDEFINED,
        desiredQuality: Int = 80,
    ): Result<ImageCompressionResult> = withContext(dispatchers.io) {
        runCatching {
            val compressedBitmap = compressToBitmap(inputStreamProvider, resizeMode, orientation).getOrThrow()
            // Encode bitmap to the destination temporary file
            val tmpFile = context.createTmpFile(extension = "jpeg")
            tmpFile.outputStream().use {
                compressedBitmap.compress(format, desiredQuality, it)
            }
            ImageCompressionResult(
                file = tmpFile,
                width = compressedBitmap.width,
                height = compressedBitmap.height,
                size = tmpFile.length()
            )
        }
    }

    /**
     * Decodes the inputStream from [inputStreamProvider] into a [Bitmap] and applies the needed transformations (rotation, scale)
     * based on [resizeMode] and [orientation].
     * @return a [Result] containing the resulting [Bitmap].
     */
    fun compressToBitmap(
        inputStreamProvider: () -> InputStream,
        resizeMode: ResizeMode,
        orientation: Int,
    ): Result<Bitmap> = runCatching {
        val options = BitmapFactory.Options()
        // Decode bounds
        inputStreamProvider().use { input ->
            calculateDecodingScale(input, resizeMode, options)
        }
        // Decode the actual bitmap
        inputStreamProvider().use { input ->
            // Now read the actual image and rotate it to match its metadata
            options.inJustDecodeBounds = false
            val decodedBitmap = BitmapFactory.decodeStream(input, null, options)
                ?: error("Decoding Bitmap from InputStream failed")
            val rotatedBitmap = decodedBitmap.rotateToMetadataOrientation(orientation)
            if (resizeMode is ResizeMode.Strict) {
                rotatedBitmap.resizeToMax(resizeMode.maxWidth, resizeMode.maxHeight)
            } else {
                rotatedBitmap
            }
        }
    }

    private fun calculateDecodingScale(
        inputStream: InputStream,
        resizeMode: ResizeMode,
        options: BitmapFactory.Options
    ) {
        val (width, height) = when (resizeMode) {
            is ResizeMode.Approximate -> resizeMode.desiredWidth to resizeMode.desiredHeight
            is ResizeMode.Strict -> resizeMode.maxWidth / 2 to resizeMode.maxHeight / 2
            is ResizeMode.None -> return
        }
        // Read bounds only
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(inputStream, null, options)
        // Set sample size based on the outWidth and outHeight
        options.inSampleSize = options.calculateInSampleSize(width, height)
    }
}

data class ImageCompressionResult(
    val file: File,
    val width: Int,
    val height: Int,
    val size: Long,
)

sealed interface ResizeMode {
    data object None : ResizeMode
    data class Approximate(val desiredWidth: Int, val desiredHeight: Int) : ResizeMode
    data class Strict(val maxWidth: Int, val maxHeight: Int) : ResizeMode
}
