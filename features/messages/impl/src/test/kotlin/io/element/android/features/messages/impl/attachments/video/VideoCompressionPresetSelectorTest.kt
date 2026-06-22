/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.video

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class VideoCompressionPresetSelectorTest {
    private val selector = VideoCompressionPresetSelector()

    @Test
    fun `selectBestVideoPreset - returns expected preset when it can upload`() {
        val result = selector.selectBestVideoPreset(
            expectedVideoPreset = VideoCompressionPreset.HIGH,
            videoSizeEstimations = AsyncData.Success(
                persistentListOf(
                    VideoUploadEstimation(VideoCompressionPreset.HIGH, sizeInBytes = 100, canUpload = true),
                    VideoUploadEstimation(VideoCompressionPreset.STANDARD, sizeInBytes = 50, canUpload = true),
                    VideoUploadEstimation(VideoCompressionPreset.LOW, sizeInBytes = 25, canUpload = true),
                )
            )
        )

        assertThat(result.dataOrNull()).isEqualTo(VideoCompressionPreset.HIGH)
    }

    @Test
    fun `selectBestVideoPreset - falls back to the highest fitting preset`() {
        val result = selector.selectBestVideoPreset(
            expectedVideoPreset = VideoCompressionPreset.HIGH,
            videoSizeEstimations = AsyncData.Success(
                persistentListOf(
                    VideoUploadEstimation(VideoCompressionPreset.HIGH, sizeInBytes = 100, canUpload = false),
                    VideoUploadEstimation(VideoCompressionPreset.STANDARD, sizeInBytes = 50, canUpload = true),
                    VideoUploadEstimation(VideoCompressionPreset.LOW, sizeInBytes = 25, canUpload = true),
                )
            )
        )

        assertThat(result.dataOrNull()).isEqualTo(VideoCompressionPreset.STANDARD)
    }

    @Test
    fun `selectBestVideoPreset - starts from the expected preset`() {
        val result = selector.selectBestVideoPreset(
            expectedVideoPreset = VideoCompressionPreset.STANDARD,
            videoSizeEstimations = AsyncData.Success(
                persistentListOf(
                    VideoUploadEstimation(VideoCompressionPreset.HIGH, sizeInBytes = 100, canUpload = true),
                    VideoUploadEstimation(VideoCompressionPreset.STANDARD, sizeInBytes = 50, canUpload = true),
                    VideoUploadEstimation(VideoCompressionPreset.LOW, sizeInBytes = 25, canUpload = true),
                )
            )
        )

        assertThat(result.dataOrNull()).isEqualTo(VideoCompressionPreset.STANDARD)
    }

    @Test
    fun `selectBestVideoPreset - returns failure when no preset can upload`() {
        val result = selector.selectBestVideoPreset(
            expectedVideoPreset = VideoCompressionPreset.HIGH,
            videoSizeEstimations = AsyncData.Success(
                persistentListOf(
                    VideoUploadEstimation(VideoCompressionPreset.HIGH, sizeInBytes = 100, canUpload = false),
                    VideoUploadEstimation(VideoCompressionPreset.STANDARD, sizeInBytes = 50, canUpload = false),
                    VideoUploadEstimation(VideoCompressionPreset.LOW, sizeInBytes = 25, canUpload = false),
                )
            )
        )

        assertThat(result).isInstanceOf(AsyncData.Failure::class.java)
    }

    @Test
    fun `selectBestVideoPreset - returns loading while estimations are missing`() {
        val result = selector.selectBestVideoPreset(
            expectedVideoPreset = VideoCompressionPreset.HIGH,
            videoSizeEstimations = AsyncData.Loading(),
        )

        assertThat(result).isInstanceOf(AsyncData.Loading::class.java)
    }
}
