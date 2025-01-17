/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeAudio
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf

class SingleMediaGalleryDataSource(
    private val data: GroupedMediaItems,
) : MediaGalleryDataSource {
    override fun start() = Unit
    override fun groupedMediaItemsFlow() = flowOf(AsyncData.Success(data))
    override fun getLastData(): AsyncData<GroupedMediaItems> = AsyncData.Success(data)
    override suspend fun loadMore(direction: Timeline.PaginationDirection) = Unit
    override suspend fun deleteItem(eventId: EventId) = Unit

    companion object {
        fun createFrom(params: MediaViewerEntryPoint.Params) = SingleMediaGalleryDataSource(
            data = when {
                params.mediaInfo.mimeType.isMimeTypeImage() -> {
                    MediaItem.Image(
                        id = UniqueId("dummy"),
                        eventId = params.eventId,
                        mediaInfo = params.mediaInfo,
                        mediaSource = params.mediaSource,
                        thumbnailSource = params.thumbnailSource,
                    )
                }
                params.mediaInfo.mimeType.isMimeTypeVideo() -> {
                    MediaItem.Video(
                        id = UniqueId("dummy"),
                        eventId = params.eventId,
                        mediaInfo = params.mediaInfo,
                        mediaSource = params.mediaSource,
                        thumbnailSource = params.thumbnailSource,
                    )
                }
                params.mediaInfo.mimeType.isMimeTypeAudio() -> {
                    if (params.mediaInfo.waveform == null) {
                        MediaItem.Audio(
                            id = UniqueId("dummy"),
                            eventId = params.eventId,
                            mediaInfo = params.mediaInfo,
                            mediaSource = params.mediaSource,
                        )
                    } else {
                        MediaItem.Voice(
                            id = UniqueId("dummy"),
                            eventId = params.eventId,
                            mediaInfo = params.mediaInfo,
                            mediaSource = params.mediaSource,
                        )
                    }
                }
                else -> {
                    MediaItem.File(
                        id = UniqueId("dummy"),
                        eventId = params.eventId,
                        mediaInfo = params.mediaInfo,
                        mediaSource = params.mediaSource,
                    )
                }
            }.let { mediaItem ->
                GroupedMediaItems(
                    // Always use imageAndVideoItems, in Single mode, this is the data that will be used
                    imageAndVideoItems = persistentListOf(mediaItem),
                    fileItems = persistentListOf(),
                )
            }
        )
    }
}
