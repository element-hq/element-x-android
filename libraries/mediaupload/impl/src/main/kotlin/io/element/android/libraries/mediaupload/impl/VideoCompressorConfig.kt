/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import androidx.annotation.OptIn
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.VideoEncoderSettings
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
internal object VideoCompressorConfigFactory {
    // Major dimension of 720p
    private const val MAX_COMPRESSED_PIXEL_SIZE = 1280

    // Major dimension of 1080p
    private const val MAX_PIXEL_SIZE = 1920

    private const val DEFAULT_FRAME_RATE = 30

    fun create(
        metadata: VideoFileMetadata?,
        shouldBeCompressed: Boolean,
    ): VideoCompressorConfig {
        val width = metadata?.width?.takeIf { it >= 0 } ?: Int.MAX_VALUE
        val height = metadata?.height?.takeIf { it >= 0 } ?: Int.MAX_VALUE
        val originalBitrate = metadata?.bitrate?.takeIf { it >= 0 }
        val originalFrameRate = metadata?.frameRate?.takeIf { it >= 0 } ?: DEFAULT_FRAME_RATE

        // We only create a resizer if needed
        val resizer = when {
            shouldBeCompressed && (width > MAX_COMPRESSED_PIXEL_SIZE || height > MAX_COMPRESSED_PIXEL_SIZE) -> VideoResizer(MAX_COMPRESSED_PIXEL_SIZE)
            width > MAX_PIXEL_SIZE || height > MAX_PIXEL_SIZE -> VideoResizer(MAX_PIXEL_SIZE)
            else -> null
        }

        // If we are resizing, we also want to reduce the frame rate to the default value (30fps)
        val newFrameRate = if (resizer is VideoResizer) {
            min(originalFrameRate, DEFAULT_FRAME_RATE)
        } else {
            originalFrameRate
        }

        // If we need to resize the video, we also want to recalculate the bitrate
        val newBitrate = if (resizer is VideoResizer) {
            val maxSize = resizer.getOutputSize(Size(width, height))
            val pixelsPerFrame = maxSize.width * maxSize.height
            val frameRate = newFrameRate
            // Apparently, 0.1 bits per pixel is a sweet spot for video compression
            val bitsPerPixel = 0.1f

            (pixelsPerFrame * bitsPerPixel * frameRate).toLong()
        } else {
            originalBitrate
        }

        return VideoCompressorConfig(
            resizer = resizer,
            newBitRate = newBitrate?.toInt() ?: VideoEncoderSettings.NO_VALUE,
            newFrameRate = newFrameRate,
        )
    }
}

@OptIn(UnstableApi::class)
internal data class VideoCompressorConfig(
    val resizer: VideoResizer?,
    val newBitRate: Int,
    val newFrameRate: Int,
)

@OptIn(UnstableApi::class)
internal class VideoResizer(
    val maxSize: Int,
) {
    fun getOutputSize(inputSize: Size): Size {
        val resultMajor = min(inputSize.major(), maxSize)
        val aspectRatio = inputSize.major().toFloat() / inputSize.minor().toFloat()
        return Size(resultMajor, (resultMajor / aspectRatio).roundToInt())
    }
}

@OptIn(UnstableApi::class)
internal fun Size.major(): Int = if (width > height) width else height

@OptIn(UnstableApi::class)
internal fun Size.minor(): Int = if (width < height) width else height
