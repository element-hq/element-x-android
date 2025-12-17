/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.media.WaveFormSamples
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.media.aMediaSource
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.mediaviewer.api.aVideoMediaInfo
import io.element.android.libraries.mediaviewer.api.aVoiceMediaInfo
import io.element.android.libraries.mediaviewer.api.anApkMediaInfo
import io.element.android.libraries.mediaviewer.api.anAudioMediaInfo
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo
import io.element.android.libraries.mediaviewer.impl.gallery.aGroupedMediaItems
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemFile
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemImage
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SingleMediaGalleryDataSourceTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `function start is no op`() {
        val sut = SingleMediaGalleryDataSource(aGroupedMediaItems())
        sut.start()
    }

    @Test
    fun `function loadMore is no op`() = runTest {
        val sut = SingleMediaGalleryDataSource(aGroupedMediaItems())
        sut.loadMore(Timeline.PaginationDirection.BACKWARDS)
        sut.loadMore(Timeline.PaginationDirection.FORWARDS)
    }

    @Test
    fun `function deleteItem is no op`() = runTest {
        val sut = SingleMediaGalleryDataSource(aGroupedMediaItems())
        sut.deleteItem(AN_EVENT_ID)
    }

    @Test
    fun `getLastData should return the data`() {
        val data = aGroupedMediaItems(
            imageAndVideoItems = listOf(aMediaItemImage()),
            fileItems = listOf(aMediaItemFile()),
        )
        val sut = SingleMediaGalleryDataSource(data)
        assertThat(sut.getLastData()).isEqualTo(AsyncData.Success(data))
    }

    @Test
    fun `groupedMediaItemsFlow emit a single item`() = runTest {
        val data = aGroupedMediaItems(
            imageAndVideoItems = listOf(aMediaItemImage()),
            fileItems = listOf(aMediaItemFile()),
        )
        val sut = SingleMediaGalleryDataSource(data)
        sut.groupedMediaItemsFlow().test {
            assertThat(awaitItem()).isEqualTo(AsyncData.Success(data))
            awaitComplete()
        }
    }

    @Test
    fun `createFrom should create a SingleMediaGalleryDataSource with an image item`() {
        testFactory(
            mediaInfo = anImageMediaInfo(),
            expectedResult = { params ->
                MediaItem.Image(
                    id = UniqueId("dummy"),
                    eventId = params.eventId,
                    mediaInfo = params.mediaInfo,
                    mediaSource = params.mediaSource,
                    thumbnailSource = params.thumbnailSource,
                )
            }
        )
    }

    @Test
    fun `createFrom should create a SingleMediaGalleryDataSource with a video item`() {
        testFactory(
            mediaInfo = aVideoMediaInfo(),
            expectedResult = { params ->
                MediaItem.Video(
                    id = UniqueId("dummy"),
                    eventId = params.eventId,
                    mediaInfo = params.mediaInfo,
                    mediaSource = params.mediaSource,
                    thumbnailSource = params.thumbnailSource,
                )
            }
        )
    }

    @Test
    fun `createFrom should create a SingleMediaGalleryDataSource with an audio item`() {
        testFactory(
            mediaInfo = anAudioMediaInfo(),
            expectedResult = { params ->
                MediaItem.Audio(
                    id = UniqueId("dummy"),
                    eventId = params.eventId,
                    mediaInfo = params.mediaInfo,
                    mediaSource = params.mediaSource,
                )
            }
        )
    }

    @Test
    fun `createFrom should create a SingleMediaGalleryDataSource with a voice item`() {
        testFactory(
            mediaInfo = aVoiceMediaInfo(
                waveForm = WaveFormSamples.longRealisticWaveForm,
                duration = "12:34",
            ),
            expectedResult = { params ->
                MediaItem.Voice(
                    id = UniqueId("dummy"),
                    eventId = params.eventId,
                    mediaInfo = params.mediaInfo,
                    mediaSource = params.mediaSource,
                )
            }
        )
    }

    @Test
    fun `createFrom should create a SingleMediaGalleryDataSource with a file item`() {
        testFactory(
            mediaInfo = anApkMediaInfo(),
            expectedResult = { params ->
                MediaItem.File(
                    id = UniqueId("dummy"),
                    eventId = params.eventId,
                    mediaInfo = params.mediaInfo,
                    mediaSource = params.mediaSource,
                )
            }
        )
    }

    private fun testFactory(
        mediaInfo: MediaInfo,
        expectedResult: (MediaViewerEntryPoint.Params) -> MediaItem,
    ) {
        val params = aMediaViewerEntryPointParams(mediaInfo)
        val result = SingleMediaGalleryDataSource.createFrom(params)
        val resultData = result.getLastData().dataOrNull()
        assertThat(resultData!!.imageAndVideoItems.first()).isEqualTo(expectedResult(params))
        assertThat(resultData.fileItems).isEmpty()
    }

    internal fun aMediaViewerEntryPointParams(
        mediaInfo: MediaInfo,
    ) = MediaViewerEntryPoint.Params(
        mode = MediaViewerEntryPoint.MediaViewerMode.SingleMedia,
        eventId = AN_EVENT_ID,
        mediaInfo = mediaInfo,
        mediaSource = aMediaSource(url = "aUrl"),
        thumbnailSource = aMediaSource(url = "aThumbnailUrl"),
        canShowInfo = true,
    )
}
