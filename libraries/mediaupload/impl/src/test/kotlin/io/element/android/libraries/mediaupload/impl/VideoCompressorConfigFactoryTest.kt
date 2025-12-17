/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import androidx.media3.transformer.VideoEncoderSettings
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("NOTHING_TO_INLINE")
@RunWith(RobolectricTestRunner::class)
class VideoCompressorConfigFactoryTest {
    @Test
    fun `if we don't have metadata the video will be resized`() {
        // Given
        val metadata = null
        val preset = VideoCompressionPreset.STANDARD

        // When
        val videoCompressorConfig = VideoCompressorConfigFactory.create(
            metadata = metadata,
            preset = preset,
        )

        // Then
        assertThat(videoCompressorConfig.videoCompressorHelper).isNotNull()
        assertThat(videoCompressorConfig.newFrameRate).isEqualTo(30)
        assertThat(videoCompressorConfig.newBitRate).isNotEqualTo(VideoEncoderSettings.NO_VALUE)
    }

    @Test
    fun `if the video should be compressed and is larger than 720p it will be resized`() {
        // Given
        val metadata = VideoFileMetadata(width = 1920, height = 1080, bitrate = 1_000_000, frameRate = 50, rotation = 0)
        val preset = VideoCompressionPreset.STANDARD

        // When
        val videoCompressorConfig = VideoCompressorConfigFactory.create(
            metadata = metadata,
            preset = preset,
        )

        // Then
        assertIsResized(videoCompressorConfig, metadata.width)
    }

    @Test
    fun `if the video should be compressed and is smaller or equal to 720p it will not be resized`() {
        // Given
        val metadata = VideoFileMetadata(width = 1280, height = 720, bitrate = 1_000_000, frameRate = 50, rotation = 0)
        val preset = VideoCompressionPreset.STANDARD

        // When
        val videoCompressorConfig = VideoCompressorConfigFactory.create(
            metadata = metadata,
            preset = preset,
        )

        // Then
        assertIsNotResized(videoCompressorConfig, 1280)
    }

    @Test
    fun `if the video should not be compressed and is larger than 1080p it will be resized`() {
        // Given
        val metadata = VideoFileMetadata(width = 2560, height = 1440, bitrate = 1_000_000, frameRate = 50, rotation = 0)
        val preset = VideoCompressionPreset.HIGH

        // When
        val videoCompressorConfig = VideoCompressorConfigFactory.create(
            metadata = metadata,
            preset = preset,
        )

        // Then
        assertIsResized(videoCompressorConfig, metadata.width)
    }

    @Test
    fun `if the video should not be compressed and is smaller or equal than 1080p it will not be resized`() {
        // Given
        val metadata = VideoFileMetadata(width = 1920, height = 1080, bitrate = 1_000_000, frameRate = 50, rotation = 0)
        val preset = VideoCompressionPreset.HIGH

        // When
        val videoCompressorConfig = VideoCompressorConfigFactory.create(
            metadata = metadata,
            preset = preset,
        )

        // Then
        assertIsNotResized(videoCompressorConfig, 1920)
    }

    private inline fun assertIsResized(videoCompressorConfig: VideoCompressorConfig, referenceSize: Int) {
        assertThat(videoCompressorConfig.videoCompressorHelper.maxSize).isNotEqualTo(referenceSize)
    }

    private inline fun assertIsNotResized(videoCompressorConfig: VideoCompressorConfig, referenceSize: Int) {
        assertThat(videoCompressorConfig.videoCompressorHelper.maxSize).isEqualTo(referenceSize)
    }
}
