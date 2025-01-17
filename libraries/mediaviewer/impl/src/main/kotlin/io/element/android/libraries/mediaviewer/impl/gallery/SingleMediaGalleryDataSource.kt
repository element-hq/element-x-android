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
import kotlinx.collections.immutable.toImmutableList
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
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(
                            MediaItem.Image(
                                id = UniqueId("dummy"),
                                eventId = params.eventId,
                                mediaInfo = params.mediaInfo,
                                mediaSource = params.mediaSource,
                                thumbnailSource = params.thumbnailSource,
                            )
                        ),
                        fileItems = persistentListOf(),
                    )
                }
                params.mediaInfo.mimeType.isMimeTypeVideo() -> {
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(
                            MediaItem.Video(
                                id = UniqueId("dummy"),
                                eventId = params.eventId,
                                mediaInfo = params.mediaInfo,
                                mediaSource = params.mediaSource,
                                thumbnailSource = params.thumbnailSource,
                                duration = "TODO", // TODO Duration
                            )
                        ),
                        fileItems = persistentListOf(),
                    )
                }
                params.mediaInfo.mimeType.isMimeTypeAudio() -> {
                    if (params.mediaInfo.waveform == null) {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(
                                MediaItem.Audio(
                                    id = UniqueId("dummy"),
                                    eventId = params.eventId,
                                    mediaInfo = params.mediaInfo,
                                    mediaSource = params.mediaSource,
                                )
                            ),
                            fileItems = persistentListOf(),
                        )
                    } else {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(
                                MediaItem.Voice(
                                    id = UniqueId("dummy"),
                                    eventId = params.eventId,
                                    mediaInfo = params.mediaInfo,
                                    mediaSource = params.mediaSource,
                                    duration = "TODO", // TODO Duration
                                    waveform = params.mediaInfo.waveform.orEmpty().toImmutableList(),
                                )
                            ),
                            fileItems = persistentListOf(),
                        )
                    }
                }
                else -> {
                    // Always use imageAndVideoItems, in Single mode, this is the data that will be used
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(
                            MediaItem.File(
                                id = UniqueId("dummy"),
                                eventId = params.eventId,
                                mediaInfo = params.mediaInfo,
                                mediaSource = params.mediaSource,
                            )
                        ),
                        fileItems = persistentListOf(),
                    )
                }
            }
        )
    }
}
