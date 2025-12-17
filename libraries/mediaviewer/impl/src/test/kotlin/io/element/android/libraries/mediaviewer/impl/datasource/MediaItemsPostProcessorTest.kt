/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.datasource

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.mediaviewer.impl.model.GroupedMediaItems
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemAudio
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemDateSeparator
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemFile
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemImage
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemLoadingIndicator
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemVideo
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemVoice
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class MediaItemsPostProcessorTest {
    private val file1 = aMediaItemFile(id = UniqueId("1"))
    private val file2 = aMediaItemFile(id = UniqueId("2"))
    private val file3 = aMediaItemFile(id = UniqueId("3"))
    private val audio1 = aMediaItemAudio(id = UniqueId("1"))
    private val audio2 = aMediaItemAudio(id = UniqueId("2"))
    private val audio3 = aMediaItemAudio(id = UniqueId("3"))
    private val voice1 = aMediaItemVoice(id = UniqueId("1"))
    private val voice2 = aMediaItemVoice(id = UniqueId("2"))
    private val voice3 = aMediaItemVoice(id = UniqueId("3"))
    private val image1 = aMediaItemImage(id = UniqueId("1"))
    private val image2 = aMediaItemImage(id = UniqueId("2"))
    private val image3 = aMediaItemImage(id = UniqueId("3"))
    private val video1 = aMediaItemVideo(id = UniqueId("1"))
    private val video2 = aMediaItemVideo(id = UniqueId("2"))
    private val video3 = aMediaItemVideo(id = UniqueId("3"))
    private val date1 = aMediaItemDateSeparator(id = UniqueId("1"))
    private val date2 = aMediaItemDateSeparator(id = UniqueId("2"))
    private val date3 = aMediaItemDateSeparator(id = UniqueId("3"))
    private val loading1 = aMediaItemLoadingIndicator(id = UniqueId("1"))

    @Test
    fun `process Empty`() {
        test(
            mediaItems = listOf(),
            expectedImageAndVideoItems = emptyList(),
            expectedFileItems = emptyList(),
        )
    }

    @Test
    fun `process will reorder files`() {
        test(
            mediaItems = listOf(
                audio1,
                file3,
                file2,
                file1,
                date1,
            ),
            expectedImageAndVideoItems = emptyList(),
            expectedFileItems = listOf(
                date1,
                audio1,
                file3,
                file2,
                file1,
            ),
        )
    }

    @Test
    fun `process will reorder images`() {
        test(
            mediaItems = listOf(
                image3,
                image2,
                image1,
                date1,
            ),
            expectedImageAndVideoItems = listOf(
                date1,
                image3,
                image2,
                image1,
            ),
            expectedFileItems = emptyList(),
        )
    }

    @Test
    fun `process will split images, videos and files`() {
        test(
            mediaItems = listOf(
                audio1,
                file1,
                image1,
                video1,
                date1,
            ),
            expectedImageAndVideoItems = listOf(
                date1,
                image1,
                video1,
            ),
            expectedFileItems = listOf(
                date1,
                audio1,
                file1,
            ),
        )
    }

    @Test
    fun `process will skip date if there is no items`() {
        test(
            mediaItems = listOf(
                date1,
                date2,
                date3,
            ),
            expectedImageAndVideoItems = emptyList(),
            expectedFileItems = emptyList(),
        )
    }

    @Test
    fun `process will add the loading indicator to both list`() {
        test(
            mediaItems = listOf(
                loading1,
            ),
            expectedImageAndVideoItems = listOf(
                loading1,
            ),
            expectedFileItems = listOf(
                loading1,
            ),
        )
    }

    @Test
    fun `process will handle complex case`() {
        test(
            mediaItems = listOf(
                file3,
                date3,
                video3,
                video2,
                date2,
                voice3,
                voice2,
                voice1,
                audio3,
                audio2,
                audio1,
                file1,
                image1,
                video1,
                date1,
                loading1,
            ),
            expectedImageAndVideoItems = listOf(
                date2,
                video3,
                video2,
                date1,
                image1,
                video1,
                loading1,
            ),
            expectedFileItems = listOf(
                date3,
                file3,
                date1,
                voice3,
                voice2,
                voice1,
                audio3,
                audio2,
                audio1,
                file1,
                loading1,
            ),
        )
    }

    private fun test(
        mediaItems: List<MediaItem>,
        expectedImageAndVideoItems: List<MediaItem>,
        expectedFileItems: List<MediaItem>,
    ) {
        val sut = MediaItemsPostProcessor()
        val result = sut.process(mediaItems.toImmutableList())

        // Compare the lists to have better failure info
        assertThat(result.imageAndVideoItems.toList()).isEqualTo(expectedImageAndVideoItems)
        assertThat(result.fileItems.toList()).isEqualTo(expectedFileItems)

        assertThat(result).isEqualTo(
            GroupedMediaItems(
                imageAndVideoItems = expectedImageAndVideoItems.toImmutableList(),
                fileItems = expectedFileItems.toImmutableList(),
            )
        )
    }
}
