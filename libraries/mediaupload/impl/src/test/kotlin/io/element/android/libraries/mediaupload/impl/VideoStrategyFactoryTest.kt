/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy
import com.otaliastudios.transcoder.strategy.PassThroughTrackStrategy
import com.otaliastudios.transcoder.strategy.TrackStrategy
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("NOTHING_TO_INLINE")
@RunWith(RobolectricTestRunner::class)
class VideoStrategyFactoryTest {
    @Test
    fun `if we don't have metadata the video will be transcoded just in case`() {
        // Given
        val expectedExtension = "mp4"
        val metadata = null
        val shouldBeCompressed = true

        // When
        val videoStrategy = VideoStrategyFactory.create(
            expectedExtension = expectedExtension,
            metadata = metadata,
            shouldBeCompressed = shouldBeCompressed
        )

        // Then
        assertIsTranscoded(videoStrategy)
    }

    @Test
    fun `if the video should be compressed and is larger than 720p it will be transcoded`() {
        // Given
        val expectedExtension = "mp4"
        val metadata = VideoFileMetadata(width = 1920, height = 1080, bitrate = 1_000_000, frameRate = 50)
        val shouldBeCompressed = true

        // When
        val videoStrategy = VideoStrategyFactory.create(
            expectedExtension = expectedExtension,
            metadata = metadata,
            shouldBeCompressed = shouldBeCompressed
        )

        // Then
        assertIsTranscoded(videoStrategy)
    }

    @Test
    fun `if the video should be compressed, has the right format and is smaller or equal to 720p it will not be transcoded`() {
        // Given
        val expectedExtension = "mp4"
        val metadata = VideoFileMetadata(width = 1280, height = 720, bitrate = 1_000_000, frameRate = 50)
        val shouldBeCompressed = true

        // When
        val videoStrategy = VideoStrategyFactory.create(
            expectedExtension = expectedExtension,
            metadata = metadata,
            shouldBeCompressed = shouldBeCompressed
        )

        // Then
        assertIsNotTranscoded(videoStrategy)
    }

    @Test
    fun `if the video should not be compressed and is larger than 1080p it will be transcoded`() {
        // Given
        val expectedExtension = "mp4"
        val metadata = VideoFileMetadata(width = 2560, height = 1440, bitrate = 1_000_000, frameRate = 50)
        val shouldBeCompressed = false

        // When
        val videoStrategy = VideoStrategyFactory.create(
            expectedExtension = expectedExtension,
            metadata = metadata,
            shouldBeCompressed = shouldBeCompressed
        )

        // Then
        assertIsTranscoded(videoStrategy)
    }

    @Test
    fun `if the video should not be compressed, has the right format and is smaller or equal than 1080p it will not be transcoded`() {
        // Given
        val expectedExtension = "mp4"
        val metadata = VideoFileMetadata(width = 1920, height = 1080, bitrate = 1_000_000, frameRate = 50)
        val shouldBeCompressed = false

        // When
        val videoStrategy = VideoStrategyFactory.create(
            expectedExtension = expectedExtension,
            metadata = metadata,
            shouldBeCompressed = shouldBeCompressed
        )

        // Then
        assertIsNotTranscoded(videoStrategy)
    }

    @Test
    fun `if the video should not be compressed but has a wrong format it will be transcoded`() {
        // Given
        val expectedExtension = "mkv"
        val metadata = VideoFileMetadata(width = 320, height = 240, bitrate = 1_000_000, frameRate = 50)
        val shouldBeCompressed = false

        // When
        val videoStrategy = VideoStrategyFactory.create(
            expectedExtension = expectedExtension,
            metadata = metadata,
            shouldBeCompressed = shouldBeCompressed
        )

        // Then
        assertIsTranscoded(videoStrategy)
    }

    @Test
    fun `if the video should be compressed and has a wrong format it will be transcoded`() {
        // Given
        val expectedExtension = "mkv"
        val metadata = VideoFileMetadata(width = 320, height = 240, bitrate = 1_000_000, frameRate = 50)
        val shouldBeCompressed = true

        // When
        val videoStrategy = VideoStrategyFactory.create(
            expectedExtension = expectedExtension,
            metadata = metadata,
            shouldBeCompressed = shouldBeCompressed
        )

        // Then
        assertIsTranscoded(videoStrategy)
    }

    private inline fun assertIsTranscoded(videoStrategy: TrackStrategy) {
        assert(videoStrategy is DefaultVideoStrategy)
    }

    private inline fun assertIsNotTranscoded(videoStrategy: TrackStrategy) {
        assert(videoStrategy is PassThroughTrackStrategy)
    }
}
