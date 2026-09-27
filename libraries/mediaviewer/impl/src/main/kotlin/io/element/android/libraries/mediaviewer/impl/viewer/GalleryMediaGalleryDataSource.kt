/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaviewer.api.GalleryInfo
import io.element.android.libraries.mediaviewer.api.GalleryItemData
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.impl.datasource.MediaGalleryDataSource
import io.element.android.libraries.mediaviewer.impl.model.GroupedMediaItems
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf

class GalleryMediaGalleryDataSource(
    private val data: GroupedMediaItems,
) : MediaGalleryDataSource {
    override val isReady: Boolean = true
    override fun start(coroutineScope: CoroutineScope) = Unit
    override fun groupedMediaItemsFlow() = flowOf(AsyncData.Success(data))
    override fun getLastData(): AsyncData<GroupedMediaItems> = AsyncData.Success(data)
    override suspend fun loadMore(direction: Timeline.PaginationDirection) = Unit
    override suspend fun deleteItem(eventId: EventId) = Unit

    companion object {
        fun createFrom(
            eventId: EventId?,
            galleryItems: List<GalleryItemData>,
            galleryInfo: GalleryInfo,
        ): GalleryMediaGalleryDataSource {
            val mixedItems = mutableListOf<MediaItem.Event>()
            galleryItems.forEachIndexed { index, galleryItem ->
                val itemMediaInfo = MediaInfo(
                    filename = galleryItem.filename,
                    fileSize = null,
                    caption = galleryInfo.caption,
                    mimeType = galleryItem.mimeType,
                    formattedFileSize = "",
                    fileExtension = galleryItem.filename.substringAfterLast('.', ""),
                    senderId = galleryInfo.senderId,
                    senderName = galleryInfo.senderName,
                    senderAvatar = galleryInfo.senderAvatar,
                    dateSent = galleryInfo.dateSent,
                    dateSentFull = galleryInfo.dateSentFull,
                    waveform = null,
                    duration = null,
                )
                val id = UniqueId("${eventId?.value ?: "gallery"}_$index")
                val mediaItem: MediaItem.Event = when (galleryItem.type) {
                    GalleryItemData.Type.Video -> MediaItem.Video(
                        id = id,
                        eventId = eventId,
                        mediaInfo = itemMediaInfo,
                        mediaSource = galleryItem.mediaSource,
                        thumbnailSource = galleryItem.thumbnailSource,
                    )
                    GalleryItemData.Type.Audio -> MediaItem.Audio(
                        id = id,
                        eventId = eventId,
                        mediaInfo = itemMediaInfo,
                        mediaSource = galleryItem.mediaSource,
                    )
                    GalleryItemData.Type.File -> MediaItem.File(
                        id = id,
                        eventId = eventId,
                        mediaInfo = itemMediaInfo,
                        mediaSource = galleryItem.mediaSource,
                    )
                    GalleryItemData.Type.Image -> MediaItem.Image(
                        id = id,
                        eventId = eventId,
                        mediaInfo = itemMediaInfo,
                        mediaSource = galleryItem.mediaSource,
                        thumbnailSource = galleryItem.thumbnailSource,
                    )
                }
                mixedItems.add(mediaItem)
            }
            return GalleryMediaGalleryDataSource(
                data = GroupedMediaItems(
                    imageAndVideoItems = mixedItems.toImmutableList(),
                    fileItems = persistentListOf(),
                )
            )
        }
    }
}
