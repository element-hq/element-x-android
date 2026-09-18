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
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
@Suppress("NOTHING_TO_INLINE")
class VideoCompressorConfigFactoryTest : RobolectricTest() {
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
    fun `the bitrate is clamped to the source bitrate when the source is already low`() {
        val metadata = VideoFileMetadata(width = 720, height = 720, bitrate = 615_000, frameRate = 25, rotation = 0)

        val videoCompressorConfig = VideoCompressorConfigFactory.create(
            metadata = metadata,
            preset = VideoCompressionPreset.STANDARD,
        )

        assertThat(videoCompressorConfig.newBitRate).isEqualTo(615_000)
    }

    @Test
    fun `the bitrate is not raised for a high bitrate source`() {
        val metadata = VideoFileMetadata(width = 1920, height = 1080, bitrate = 20_000_000, frameRate = 30, rotation = 0)

        val videoCompressorConfig = VideoCompressorConfigFactory.create(
            metadata = metadata,
            preset = VideoCompressionPreset.STANDARD,
        )

        assertThat(videoCompressorConfig.newBitRate).isLessThan(20_000_000)
        assertThat(videoCompressorConfig.newBitRate).isGreaterThan(0)
    }

    @Test
    fun `an unknown bitrate falls back to the calculated optimal`() {
        val unknown = VideoCompressorConfigFactory.create(
            metadata = VideoFileMetadata(width = 1280, height = 720, bitrate = -1, frameRate = 30, rotation = 0),
            preset = VideoCompressionPreset.STANDARD,
        )
        val zero = VideoCompressorConfigFactory.create(
            metadata = VideoFileMetadata(width = 1280, height = 720, bitrate = 0, frameRate = 30, rotation = 0),
            preset = VideoCompressionPreset.STANDARD,
        )

        assertThat(unknown.newBitRate).isGreaterThan(0)
        assertThat(zero.newBitRate).isEqualTo(unknown.newBitRate)
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

    @Test
    fun `square crop bitrate is based on the output square not the letterboxed source`() {
        val metadata = VideoFileMetadata(width = 1920, height = 1080, bitrate = 8_000_000, frameRate = 30, rotation = 0)

        val config = VideoCompressorConfigFactory.create(
            metadata = metadata,
            preset = VideoCompressionPreset.STANDARD,
            squareCropTo = 512,
        )

        // 512 * 512 * 0.1 * 30
        assertThat(config.newBitRate).isEqualTo(786_432)
        assertThat(config.videoCompressorHelper.maxSize).isEqualTo(512)
    }

    @Test
    fun `square crop smaller than the cap keeps the cropped side and does not upscale bitrate`() {
        val metadata = VideoFileMetadata(width = 640, height = 480, bitrate = 2_000_000, frameRate = 30, rotation = 0)

        val config = VideoCompressorConfigFactory.create(
            metadata = metadata,
            preset = VideoCompressionPreset.STANDARD,
            squareCropTo = 512,
        )

        // 480 * 480 * 0.1 * 30
        assertThat(config.newBitRate).isEqualTo(691_200)
        assertThat(config.videoCompressorHelper.maxSize).isEqualTo(480)
    }

    private inline fun assertIsResized(videoCompressorConfig: VideoCompressorConfig, referenceSize: Int) {
        assertThat(videoCompressorConfig.videoCompressorHelper.maxSize).isNotEqualTo(referenceSize)
    }

    private inline fun assertIsNotResized(videoCompressorConfig: VideoCompressorConfig, referenceSize: Int) {
        assertThat(videoCompressorConfig.videoCompressorHelper.maxSize).isEqualTo(referenceSize)
    }
}
