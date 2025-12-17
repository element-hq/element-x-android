/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import java.io.File
import kotlin.math.min

fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}

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

/**
 * Decodes the [inputStream] into a [Bitmap] and applies the needed rotation based on [orientation].
 * This orientation value must be one of `ExifInterface.ORIENTATION_*` constants.
 */
fun Bitmap.rotateToExifMetadataOrientation(orientation: Int): Bitmap {
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
        else -> return this
    }

    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
