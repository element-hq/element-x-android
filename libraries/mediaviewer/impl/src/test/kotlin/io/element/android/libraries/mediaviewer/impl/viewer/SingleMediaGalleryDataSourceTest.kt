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
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.media.aMediaSource
import io.element.android.libraries.matrix.ui.media.contentvalidation.NoopEventContentValidationCache
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

    private val contentValidationCache = NoopEventContentValidationCache()

    @Test
    fun `function start is no op`() = runTest {
        val sut = SingleMediaGalleryDataSource(aGroupedMediaItems(), contentValidationCache)
        sut.start(backgroundScope)
    }

    @Test
    fun `function loadMore is no op`() = runTest {
        val sut = SingleMediaGalleryDataSource(aGroupedMediaItems(), contentValidationCache)
        sut.loadMore(Timeline.PaginationDirection.BACKWARDS)
        sut.loadMore(Timeline.PaginationDirection.FORWARDS)
    }

    @Test
    fun `function deleteItem is no op`() = runTest {
        val sut = SingleMediaGalleryDataSource(aGroupedMediaItems(), contentValidationCache)
        sut.deleteItem(AN_EVENT_ID)
    }

    @Test
    fun `getLastData should return the data`() {
        val data = aGroupedMediaItems(
            imageAndVideoItems = listOf(aMediaItemImage()),
            fileItems = listOf(aMediaItemFile()),
        )
        val sut = SingleMediaGalleryDataSource(data, contentValidationCache)
        assertThat(sut.getLastData()).isEqualTo(AsyncData.Success(data))
    }

    @Test
    fun `groupedMediaItemsFlow emit a single item`() = runTest {
        val data = aGroupedMediaItems(
            imageAndVideoItems = listOf(aMediaItemImage()),
            fileItems = listOf(aMediaItemFile()),
        )
        val sut = SingleMediaGalleryDataSource(data, contentValidationCache)
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
                    blurHash = null,
                    validationState = contentValidationCache[AN_EVENT_ID],
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
                    blurHash = null,
                    validationState = contentValidationCache[AN_EVENT_ID],
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
                    validationState = contentValidationCache[AN_EVENT_ID],
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
                    validationState = contentValidationCache[AN_EVENT_ID],
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
                    validationState = contentValidationCache[AN_EVENT_ID],
                )
            }
        )
    }

    private fun testFactory(
        mediaInfo: MediaInfo,
        expectedResult: (MediaViewerEntryPoint.Params.RoomMedia) -> MediaItem,
    ) {
        val params = aMediaViewerEntryPointParams(mediaInfo)
        val result = params.toMediaItem(contentValidationCache[AN_EVENT_ID],)
        assertThat(result).isEqualTo(expectedResult(params))
    }

    @Test
    fun `createFrom a room media without an event id returns the image it was built from`() {
        val params = aMediaViewerEntryPointParams(anImageMediaInfo(), eventId = null)

        val sut = SingleMediaGalleryDataSource.createFrom(params, contentValidationCache)

        val data = sut.getLastData().dataOrNull()!!
        assertThat(data.fileItems).isEmpty()
        assertThat(data.imageAndVideoItems).hasSize(1)
        val item = data.imageAndVideoItems.first() as MediaItem.Image
        assertThat(item.eventId).isNull()
        assertThat(item.mediaSource).isEqualTo(params.mediaSource)
    }

    @Test
    fun `createFrom a room media without an event id puts a file in the file items`() {
        val params = aMediaViewerEntryPointParams(
            anApkMediaInfo(),
            eventId = null,
            mode = MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios(Timeline.Mode.Media),
        )

        val sut = SingleMediaGalleryDataSource.createFrom(params, contentValidationCache)

        val data = sut.getLastData().dataOrNull()!!
        assertThat(data.imageAndVideoItems).isEmpty()
        assertThat(data.fileItems).hasSize(1)
        val item = data.fileItems.first() as MediaItem.File
        assertThat(item.eventId).isNull()
        assertThat(item.mediaSource).isEqualTo(params.mediaSource)
    }

    @Test
    fun `createFrom a room media without an event id puts an audio message in the file items`() {
        val params = aMediaViewerEntryPointParams(
            anAudioMediaInfo(),
            eventId = null,
            mode = MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios(Timeline.Mode.Media),
        )

        val sut = SingleMediaGalleryDataSource.createFrom(params, contentValidationCache)

        val data = sut.getLastData().dataOrNull()!!
        assertThat(data.imageAndVideoItems).isEmpty()
        assertThat(data.fileItems).hasSize(1)
        assertThat(data.fileItems.first()).isInstanceOf(MediaItem.Audio::class.java)
    }

    internal fun aMediaViewerEntryPointParams(
        mediaInfo: MediaInfo,
        eventId: EventId? = AN_EVENT_ID,
        mode: MediaViewerEntryPoint.MediaViewerMode = MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos(Timeline.Mode.Media),
    ) = MediaViewerEntryPoint.Params.RoomMedia(
        mode = mode,
        eventId = eventId,
        mediaInfo = mediaInfo,
        mediaSource = aMediaSource(url = "aUrl"),
        thumbnailSource = aMediaSource(url = "aThumbnailUrl"),
        blurHash = null,
    )
}
