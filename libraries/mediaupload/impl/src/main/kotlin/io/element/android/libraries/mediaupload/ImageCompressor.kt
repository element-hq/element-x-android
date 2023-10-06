/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.mediaupload

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import io.element.android.libraries.androidutils.bitmap.calculateInSampleSize
import io.element.android.libraries.androidutils.bitmap.resizeToMax
import io.element.android.libraries.androidutils.bitmap.rotateToMetadataOrientation
import io.element.android.libraries.androidutils.file.createTmpFile
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context,
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
    ): Result<ImageCompressionResult> = withContext(Dispatchers.IO) {
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
