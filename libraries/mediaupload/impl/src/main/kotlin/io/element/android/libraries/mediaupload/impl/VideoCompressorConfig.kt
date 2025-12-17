/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.util.Size
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import io.element.android.libraries.androidutils.media.VideoCompressorHelper
import io.element.android.libraries.mediaupload.api.compressorHelper
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import kotlin.math.min

@OptIn(UnstableApi::class)
internal object VideoCompressorConfigFactory {
    private const val DEFAULT_FRAME_RATE = 30

    fun create(
        metadata: VideoFileMetadata?,
        preset: VideoCompressionPreset,
    ): VideoCompressorConfig {
        val width = metadata?.width?.takeIf { it >= 0 } ?: Int.MAX_VALUE
        val height = metadata?.height?.takeIf { it >= 0 } ?: Int.MAX_VALUE
        val originalFrameRate = metadata?.frameRate?.takeIf { it >= 0 } ?: DEFAULT_FRAME_RATE

        val resizer = preset.compressorHelper()

        // If we are resizing, we also want to reduce the frame rate to the default value (30fps)
        val newFrameRate = min(originalFrameRate, DEFAULT_FRAME_RATE)

        // If we need to resize the video, we also want to recalculate the bitrate
        val newBitrate = resizer.calculateOptimalBitrate(Size(width, height), newFrameRate)

        return VideoCompressorConfig(
            videoCompressorHelper = resizer,
            newBitRate = newBitrate.toInt(),
            newFrameRate = newFrameRate,
        )
    }
}

@OptIn(UnstableApi::class)
internal data class VideoCompressorConfig(
    val videoCompressorHelper: VideoCompressorHelper,
    val newBitRate: Int,
    val newFrameRate: Int,
)
