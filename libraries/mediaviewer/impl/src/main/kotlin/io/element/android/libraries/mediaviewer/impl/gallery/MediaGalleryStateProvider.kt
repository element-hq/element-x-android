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
import io.element.android.libraries.mediaviewer.impl.details.aMediaDeleteConfirmationState
import io.element.android.libraries.mediaviewer.impl.details.aMediaDetailsBottomSheetState
import io.element.android.libraries.mediaviewer.impl.gallery.ui.aDate
import io.element.android.libraries.mediaviewer.impl.gallery.ui.aFile
import io.element.android.libraries.mediaviewer.impl.gallery.ui.aVideo
import io.element.android.libraries.mediaviewer.impl.gallery.ui.anImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList

open class MediaGalleryStateProvider : PreviewParameterProvider<MediaGalleryState> {
    override val values: Sequence<MediaGalleryState>
        get() = sequenceOf(
            aMediaGalleryState(),
            aMediaGalleryState(imageAndVideoItems = AsyncData.Loading()),
            aMediaGalleryState(imageAndVideoItems = AsyncData.Success(emptyList<MediaItem.Image>().toPersistentList())),
            aMediaGalleryState(
                imageAndVideoItems = AsyncData.Success(
                    listOf(
                        aDate(id = UniqueId("0")),
                        anImage(id = UniqueId("1")),
                        aDate(
                            id = UniqueId("2"),
                            formattedDate = "September 2004",
                        ),
                        anImage(id = UniqueId("3")),
                        aVideo(id = UniqueId("4")),
                        anImage(id = UniqueId("5")),
                        anImage(id = UniqueId("6")),
                        anImage(id = UniqueId("7")),
                        anImage(id = UniqueId("8")),
                        anImage(id = UniqueId("9")),
                    ).toImmutableList()
                )
            ),
            aMediaGalleryState(mode = MediaGalleryMode.Files),
            aMediaGalleryState(mode = MediaGalleryMode.Files, fileItems = AsyncData.Loading()),
            aMediaGalleryState(mode = MediaGalleryMode.Files, fileItems = AsyncData.Success(emptyList<MediaItem.File>().toPersistentList())),
            aMediaGalleryState(mode = MediaGalleryMode.Files, fileItems = AsyncData.Success(emptyList<MediaItem.File>().toPersistentList())),
            aMediaGalleryState(
                mode = MediaGalleryMode.Files,
                fileItems = AsyncData.Success(
                    listOf(
                        aDate(id = UniqueId("0")),
                        aFile(id = UniqueId("1")),
                        aDate(
                            id = UniqueId("2"),
                            formattedDate = "September 2004",
                        ),
                        aFile(id = UniqueId("3")),
                        aFile(id = UniqueId("4")),
                        aFile(id = UniqueId("5")),
                        aFile(id = UniqueId("6")),
                    ).toImmutableList()
                )
            ),
            aMediaGalleryState(mediaBottomSheetState = aMediaDetailsBottomSheetState()),
            aMediaGalleryState(mediaBottomSheetState = aMediaDeleteConfirmationState()),
        )
}

private fun aMediaGalleryState(
    roomName: String = "Room name",
    mode: MediaGalleryMode = MediaGalleryMode.Images,
    imageAndVideoItems: AsyncData<ImmutableList<MediaItem>> = AsyncData.Uninitialized,
    fileItems: AsyncData<ImmutableList<MediaItem>> = AsyncData.Uninitialized,
    mediaBottomSheetState: MediaBottomSheetState = MediaBottomSheetState.Hidden,
) = MediaGalleryState(
    roomName = roomName,
    mode = mode,
    imageAndVideoItems = imageAndVideoItems,
    fileItems = fileItems,
    mediaBottomSheetState = mediaBottomSheetState,
    snackbarMessage = null,
    eventSink = {}
)
