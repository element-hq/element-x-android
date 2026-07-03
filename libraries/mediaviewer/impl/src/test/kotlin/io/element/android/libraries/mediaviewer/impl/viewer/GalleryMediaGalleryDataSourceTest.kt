/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.mediaviewer.api.GalleryInfo
import io.element.android.libraries.mediaviewer.api.GalleryItemData
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.impl.gallery.aGroupedMediaItems
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemFile
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemImage
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class GalleryMediaGalleryDataSourceTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `isReady is true`() {
        val sut = GalleryMediaGalleryDataSource(aGroupedMediaItems())
        assertThat(sut.isReady).isTrue()
    }

    @Test
    fun `function start is no op`() = runTest {
        val sut = GalleryMediaGalleryDataSource(aGroupedMediaItems())
        sut.start(backgroundScope)
    }

    @Test
    fun `function loadMore is no op`() = runTest {
        val sut = GalleryMediaGalleryDataSource(aGroupedMediaItems())
        sut.loadMore(Timeline.PaginationDirection.BACKWARDS)
        sut.loadMore(Timeline.PaginationDirection.FORWARDS)
    }

    @Test
    fun `function deleteItem is no op`() = runTest {
        val sut = GalleryMediaGalleryDataSource(aGroupedMediaItems())
        sut.deleteItem(AN_EVENT_ID)
    }

    @Test
    fun `getLastData should return the data`() {
        val data = aGroupedMediaItems(
            imageAndVideoItems = listOf(aMediaItemImage()),
            fileItems = listOf(aMediaItemFile()),
        )
        val sut = GalleryMediaGalleryDataSource(data)
        assertThat(sut.getLastData()).isEqualTo(AsyncData.Success(data))
    }

    @Test
    fun `groupedMediaItemsFlow emits a single item`() = runTest {
        val data = aGroupedMediaItems(
            imageAndVideoItems = listOf(aMediaItemImage()),
            fileItems = listOf(aMediaItemFile()),
        )
        val sut = GalleryMediaGalleryDataSource(data)
        sut.groupedMediaItemsFlow().test {
            assertThat(awaitItem()).isEqualTo(AsyncData.Success(data))
            awaitComplete()
        }
    }

    @Test
    fun `createFrom with image item creates MediaItem Image in imageAndVideoItems`() {
        val result = GalleryMediaGalleryDataSource.createFrom(
            eventId = AN_EVENT_ID,
            galleryItems = listOf(
                GalleryItemData(
                    filename = "image.jpg",
                    mimeType = MimeTypes.Jpeg,
                    mediaSource = MediaSource("image_url"),
                    thumbnailSource = MediaSource("thumbnail_url"),
                    type = GalleryItemData.Type.Image,
                )
            ),
            galleryInfo = aGalleryInfo(),
        )
        val data = (result.getLastData() as AsyncData.Success).data
        assertThat(data.fileItems).isEmpty()
        assertThat(data.imageAndVideoItems).containsExactly(
            MediaItem.Image(
                id = UniqueId("${AN_EVENT_ID.value}_0"),
                eventId = AN_EVENT_ID,
                mediaInfo = expectedMediaInfo("image.jpg", MimeTypes.Jpeg),
                mediaSource = MediaSource("image_url"),
                thumbnailSource = MediaSource("thumbnail_url"),
            )
        )
    }

    @Test
    fun `createFrom with video item creates MediaItem Video in imageAndVideoItems`() {
        val result = GalleryMediaGalleryDataSource.createFrom(
            eventId = AN_EVENT_ID,
            galleryItems = listOf(
                GalleryItemData(
                    filename = "video.mp4",
                    mimeType = MimeTypes.Mp4,
                    mediaSource = MediaSource("video_url"),
                    thumbnailSource = MediaSource("thumbnail_url"),
                    type = GalleryItemData.Type.Video,
                )
            ),
            galleryInfo = aGalleryInfo(),
        )
        val data = (result.getLastData() as AsyncData.Success).data
        assertThat(data.fileItems).isEmpty()
        assertThat(data.imageAndVideoItems).containsExactly(
            MediaItem.Video(
                id = UniqueId("${AN_EVENT_ID.value}_0"),
                eventId = AN_EVENT_ID,
                mediaInfo = expectedMediaInfo("video.mp4", MimeTypes.Mp4),
                mediaSource = MediaSource("video_url"),
                thumbnailSource = MediaSource("thumbnail_url"),
            )
        )
    }

    @Test
    fun `createFrom with audio item creates MediaItem Audio in imageAndVideoItems`() {
        val result = GalleryMediaGalleryDataSource.createFrom(
            eventId = AN_EVENT_ID,
            galleryItems = listOf(
                GalleryItemData(
                    filename = "audio.mp3",
                    mimeType = MimeTypes.Mp3,
                    mediaSource = MediaSource("audio_url"),
                    thumbnailSource = null,
                    type = GalleryItemData.Type.Audio,
                )
            ),
            galleryInfo = aGalleryInfo(),
        )
        val data = (result.getLastData() as AsyncData.Success).data
        assertThat(data.fileItems).isEmpty()
        assertThat(data.imageAndVideoItems).containsExactly(
            MediaItem.Audio(
                id = UniqueId("${AN_EVENT_ID.value}_0"),
                eventId = AN_EVENT_ID,
                mediaInfo = expectedMediaInfo("audio.mp3", MimeTypes.Mp3),
                mediaSource = MediaSource("audio_url"),
            )
        )
    }

    @Test
    fun `createFrom with file item creates MediaItem File in imageAndVideoItems`() {
        val result = GalleryMediaGalleryDataSource.createFrom(
            eventId = AN_EVENT_ID,
            galleryItems = listOf(
                GalleryItemData(
                    filename = "document.pdf",
                    mimeType = MimeTypes.Pdf,
                    mediaSource = MediaSource("file_url"),
                    thumbnailSource = null,
                    type = GalleryItemData.Type.File,
                )
            ),
            galleryInfo = aGalleryInfo(),
        )
        val data = (result.getLastData() as AsyncData.Success).data
        assertThat(data.fileItems).isEmpty()
        assertThat(data.imageAndVideoItems).containsExactly(
            MediaItem.File(
                id = UniqueId("${AN_EVENT_ID.value}_0"),
                eventId = AN_EVENT_ID,
                mediaInfo = expectedMediaInfo("document.pdf", MimeTypes.Pdf),
                mediaSource = MediaSource("file_url"),
            )
        )
    }

    @Test
    fun `createFrom uses gallery prefix when eventId is null`() {
        val result = GalleryMediaGalleryDataSource.createFrom(
            eventId = null,
            galleryItems = listOf(
                GalleryItemData(
                    filename = "image.jpg",
                    mimeType = MimeTypes.Jpeg,
                    mediaSource = MediaSource("image_url"),
                    thumbnailSource = null,
                    type = GalleryItemData.Type.Image,
                )
            ),
            galleryInfo = aGalleryInfo(),
        )
        val data = (result.getLastData() as AsyncData.Success).data
        val item = data.imageAndVideoItems.single() as MediaItem.Image
        assertThat(item.id).isEqualTo(UniqueId("gallery_0"))
        assertThat(item.eventId).isNull()
    }

    @Test
    fun `createFrom with multiple items produces indexed IDs and all go to imageAndVideoItems`() {
        val result = GalleryMediaGalleryDataSource.createFrom(
            eventId = AN_EVENT_ID,
            galleryItems = listOf(
                GalleryItemData(
                    filename = "image.jpg",
                    mimeType = MimeTypes.Jpeg,
                    mediaSource = MediaSource("image_url"),
                    thumbnailSource = null,
                    type = GalleryItemData.Type.Image,
                ),
                GalleryItemData(
                    filename = "document.pdf",
                    mimeType = MimeTypes.Pdf,
                    mediaSource = MediaSource("file_url"),
                    thumbnailSource = null,
                    type = GalleryItemData.Type.File,
                ),
                GalleryItemData(
                    filename = "video.mp4",
                    mimeType = MimeTypes.Mp4,
                    mediaSource = MediaSource("video_url"),
                    thumbnailSource = null,
                    type = GalleryItemData.Type.Video,
                ),
            ),
            galleryInfo = aGalleryInfo(),
        )
        val data = (result.getLastData() as AsyncData.Success).data
        assertThat(data.fileItems).isEmpty()
        assertThat(data.imageAndVideoItems).hasSize(3)
        assertThat((data.imageAndVideoItems[0] as MediaItem.Image).id).isEqualTo(UniqueId("${AN_EVENT_ID.value}_0"))
        assertThat((data.imageAndVideoItems[1] as MediaItem.File).id).isEqualTo(UniqueId("${AN_EVENT_ID.value}_1"))
        assertThat((data.imageAndVideoItems[2] as MediaItem.Video).id).isEqualTo(UniqueId("${AN_EVENT_ID.value}_2"))
    }

    private fun aGalleryInfo() = GalleryInfo(
        caption = "A caption",
        formattedCaption = null,
        senderId = A_USER_ID,
        senderName = "Alice",
        senderAvatar = null,
        dateSent = "Today",
        dateSentFull = "Today at 12:00",
        initialIndex = 0,
    )

    private fun expectedMediaInfo(filename: String, mimeType: String) = MediaInfo(
        filename = filename,
        fileSize = null,
        caption = "A caption",
        mimeType = mimeType,
        formattedFileSize = "",
        fileExtension = filename.substringAfterLast('.', ""),
        senderId = A_USER_ID,
        senderName = "Alice",
        senderAvatar = null,
        dateSent = "Today",
        dateSentFull = "Today at 12:00",
        waveform = null,
        duration = null,
    )
}
