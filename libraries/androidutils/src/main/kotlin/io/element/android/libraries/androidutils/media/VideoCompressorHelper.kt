/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.media

import android.util.Size
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Helper class to calculate the resulting output size and optimal bitrate for video compression.
 */
class VideoCompressorHelper(
    /**
     * The maximum size (in pixels) for the output video.
     * The output will maintain the aspect ratio of the input video.
     */
    val maxSize: Int,
) {
    /**
     * Calculates the output size for video compression based on the input size and [maxSize].
     */
    fun getOutputSize(inputSize: Size): Size {
        val resultMajor = min(inputSize.major(), maxSize)
        val aspectRatio = inputSize.major().toFloat() / inputSize.minor().toFloat()
        return if (inputSize.isLandscape()) {
            Size(resultMajor, (resultMajor / aspectRatio).roundToInt())
        } else {
            Size((resultMajor / aspectRatio).roundToInt(), resultMajor)
        }
    }

    /**
     * Calculates the optimal bitrate for video compression based on the input size and frame rate.
     */
    fun calculateOptimalBitrate(inputSize: Size, frameRate: Int): Long {
        val outputSize = getOutputSize(inputSize)
        val pixelsPerFrame = outputSize.width * outputSize.height
        // Apparently, 0.1 bits per pixel is a sweet spot for video compression
        val bitsPerPixel = 0.1f
        return (pixelsPerFrame * bitsPerPixel * frameRate).toLong()
    }
}

private fun Size.isLandscape(): Boolean = width > height
private fun Size.major(): Int = if (isLandscape()) width else height
private fun Size.minor(): Int = if (isLandscape()) height else width
