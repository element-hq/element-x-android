/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.libraries.androidutils.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.InputStream
import kotlin.math.min

fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}

/**
 * Reads the EXIF metadata from the [inputStream] and rotates the current [Bitmap] to match it.
 * @return The resulting [Bitmap] or `null` if no metadata was found.
 */
fun Bitmap.rotateToMetadataOrientation(inputStream: InputStream): Result<Bitmap> =
    runCatching { rotateToMetadataOrientation(this, ExifInterface(inputStream)) }

/**
 * Scales the current [Bitmap] to fit the ([maxWidth], [maxHeight]) bounds while keeping aspect ratio.
 * @throws IllegalStateException if [maxWidth] or [maxHeight] <= 0.
 */
fun Bitmap.resizeToMax(maxWidth: Int, maxHeight: Int): Bitmap {
    // No need to resize
    if (this.width == maxWidth && this.height == maxHeight) return this

    val aspectRatio = this.width.toFloat() / this.height.toFloat()
    val useWidth = aspectRatio >= 1
    val calculatedMaxWidth = min(this.width, maxWidth)
    val calculatedMinHeight = min(this.height, maxHeight)
    val width = if (useWidth) calculatedMaxWidth else (calculatedMinHeight * aspectRatio).toInt()
    val height = if (useWidth) (calculatedMaxWidth / aspectRatio).toInt() else calculatedMinHeight
    return scale(width, height)
}

/**
 * Calculates and returns [BitmapFactory.Options.inSampleSize] given a pair of [desiredWidth] & [desiredHeight]
 * and the previously read [BitmapFactory.Options.outWidth] & [BitmapFactory.Options.outHeight].
 */
fun BitmapFactory.Options.calculateInSampleSize(desiredWidth: Int, desiredHeight: Int): Int {
    var inSampleSize = 1

    if (outWidth > desiredWidth || outHeight > desiredHeight) {
        val halfHeight: Int = outHeight / 2
        val halfWidth: Int = outWidth / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= desiredHeight && halfWidth / inSampleSize >= desiredWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

private fun rotateToMetadataOrientation(bitmap: Bitmap, exifInterface: ExifInterface): Bitmap {
    val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.preRotate(-90f)
            matrix.preScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.preRotate(90f)
            matrix.preScale(-1f, 1f)
        }
        else -> return bitmap
    }

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
