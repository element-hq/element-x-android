/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaviewer.api.GalleryItemData
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.mediaviewer.impl.datasource.MediaGalleryDataSource
import io.element.android.libraries.mediaviewer.impl.model.GroupedMediaItems
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf

class GalleryMediaGalleryDataSource(
    private val data: GroupedMediaItems,
) : MediaGalleryDataSource {
    override fun start() = Unit
    override fun groupedMediaItemsFlow() = flowOf(AsyncData.Success(data))
    override fun getLastData(): AsyncData<GroupedMediaItems> = AsyncData.Success(data)
    override suspend fun loadMore(direction: Timeline.PaginationDirection) = Unit
    override suspend fun deleteItem(eventId: EventId) = Unit

    companion object {
        fun createFrom(
            eventId: EventId?,
            galleryItems: List<GalleryItemData>,
            mediaInfo: MediaInfo,
            mode: MediaViewerEntryPoint.MediaViewerMode,
        ): GalleryMediaGalleryDataSource {
            val imageAndVideoItems = mutableListOf<MediaItem.Event>()
            val fileItems = mutableListOf<MediaItem.Event>()

            galleryItems.forEachIndexed { index, galleryItem ->
                val itemMediaInfo = MediaInfo(
                    filename = galleryItem.filename,
                    fileSize = null,
                    caption = mediaInfo.caption,
                    mimeType = galleryItem.mimeType,
                    formattedFileSize = "",
                    fileExtension = galleryItem.filename.substringAfterLast('.', ""),
                    senderId = mediaInfo.senderId,
                    senderName = mediaInfo.senderName,
                    senderAvatar = mediaInfo.senderAvatar,
                    dateSent = mediaInfo.dateSent,
                    dateSentFull = mediaInfo.dateSentFull,
                    waveform = null,
                    duration = null,
                )
                val id = UniqueId("${eventId?.value ?: "gallery"}_$index")
                val mediaItem: MediaItem.Event = when {
                    galleryItem.isVideo -> MediaItem.Video(
                        id = id,
                        eventId = eventId,
                        mediaInfo = itemMediaInfo,
                        mediaSource = galleryItem.mediaSource,
                        thumbnailSource = galleryItem.thumbnailSource,
                    )
                    galleryItem.isAudio -> MediaItem.Audio(
                        id = id,
                        eventId = eventId,
                        mediaInfo = itemMediaInfo,
                        mediaSource = galleryItem.mediaSource,
                    )
                    galleryItem.isFile -> MediaItem.File(
                        id = id,
                        eventId = eventId,
                        mediaInfo = itemMediaInfo,
                        mediaSource = galleryItem.mediaSource,
                    )
                    else -> MediaItem.Image(
                        id = id,
                        eventId = eventId,
                        mediaInfo = itemMediaInfo,
                        mediaSource = galleryItem.mediaSource,
                        thumbnailSource = galleryItem.thumbnailSource,
                    )
                }
                when (mediaItem) {
                    is MediaItem.Image, is MediaItem.Video -> imageAndVideoItems.add(mediaItem)
                    is MediaItem.Audio, is MediaItem.File, is MediaItem.Voice -> fileItems.add(mediaItem)
                }
            }

            return GalleryMediaGalleryDataSource(
                data = GroupedMediaItems(
                    imageAndVideoItems = imageAndVideoItems.toImmutableList(),
                    fileItems = fileItems.toImmutableList(),
                )
            )
        }
    }
}
