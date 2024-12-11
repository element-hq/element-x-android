/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import io.element.android.libraries.mediaviewer.impl.details.aMediaDetailsBottomSheetState
import io.element.android.libraries.mediaviewer.impl.gallery.ui.aMediaItemDateSeparator
import io.element.android.libraries.mediaviewer.impl.gallery.ui.aMediaItemFile
import io.element.android.libraries.mediaviewer.impl.gallery.ui.aMediaItemImage
import io.element.android.libraries.mediaviewer.impl.gallery.ui.aMediaItemLoadingIndicator
import io.element.android.libraries.mediaviewer.impl.gallery.ui.aMediaItemVideo
import kotlinx.collections.immutable.toImmutableList

open class MediaGalleryStateProvider : PreviewParameterProvider<MediaGalleryState> {
    override val values: Sequence<MediaGalleryState>
        get() = sequenceOf(
            aMediaGalleryState(),
            aMediaGalleryState(groupedMediaItems = AsyncData.Loading()),
            aMediaGalleryState(groupedMediaItems = AsyncData.Success(aGroupedMediaItems())),
            aMediaGalleryState(
                groupedMediaItems = AsyncData.Success(
                    aGroupedMediaItems(
                        imageAndVideoItems = listOf(
                            aMediaItemDateSeparator(id = UniqueId("0")),
                            aMediaItemImage(id = UniqueId("1")),
                            aMediaItemDateSeparator(
                                id = UniqueId("2"),
                                formattedDate = "September 2004",
                            ),
                            aMediaItemImage(id = UniqueId("3")),
                            aMediaItemVideo(id = UniqueId("4")),
                            aMediaItemImage(id = UniqueId("5")),
                            aMediaItemImage(id = UniqueId("6")),
                            aMediaItemImage(id = UniqueId("7")),
                            aMediaItemImage(id = UniqueId("8")),
                            aMediaItemImage(id = UniqueId("9")),
                            aMediaItemLoadingIndicator(),
                        ).toImmutableList()
                    )
                ),
            ),
            aMediaGalleryState(mode = MediaGalleryMode.Files),
            aMediaGalleryState(mode = MediaGalleryMode.Files, groupedMediaItems = AsyncData.Loading()),
            aMediaGalleryState(mode = MediaGalleryMode.Files, groupedMediaItems = AsyncData.Success(aGroupedMediaItems())),
            aMediaGalleryState(
                mode = MediaGalleryMode.Files,
                groupedMediaItems = AsyncData.Success(
                    aGroupedMediaItems(
                        fileItems = listOf(
                            aMediaItemDateSeparator(id = UniqueId("0")),
                            aMediaItemFile(id = UniqueId("1")),
                            aMediaItemDateSeparator(
                                id = UniqueId("2"),
                                formattedDate = "September 2004",
                            ),
                            aMediaItemFile(id = UniqueId("3")),
                            aMediaItemFile(id = UniqueId("4")),
                            aMediaItemLoadingIndicator(),
                        ).toImmutableList()
                    )
                ),
            ),
            aMediaGalleryState(mediaBottomSheetState = aMediaDetailsBottomSheetState()),
            aMediaGalleryState(
                groupedMediaItems = AsyncData.Failure(Exception("Failed to load media")),
            ),
            aMediaGalleryState(
                mode = MediaGalleryMode.Files,
                groupedMediaItems = AsyncData.Failure(Exception("Failed to load media")),
            ),
        )
}

private fun aMediaGalleryState(
    roomName: String = "Room name",
    mode: MediaGalleryMode = MediaGalleryMode.Images,
    groupedMediaItems: AsyncData<GroupedMediaItems> = AsyncData.Uninitialized,
    mediaBottomSheetState: MediaBottomSheetState = MediaBottomSheetState.Hidden,
) = MediaGalleryState(
    roomName = roomName,
    mode = mode,
    groupedMediaItems = groupedMediaItems,
    mediaBottomSheetState = mediaBottomSheetState,
    snackbarMessage = null,
    eventSink = {}
)

private fun aGroupedMediaItems(
    imageAndVideoItems: List<MediaItem> = emptyList(),
    fileItems: List<MediaItem> = emptyList(),
) = GroupedMediaItems(
    imageAndVideoItems = imageAndVideoItems.toImmutableList(),
    fileItems = fileItems.toImmutableList(),
)
