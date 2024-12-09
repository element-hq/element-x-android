/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
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
            aMediaGalleryState(imageItems = AsyncData.Loading()),
            aMediaGalleryState(imageItems = AsyncData.Success(emptyList<MediaItem.Image>().toPersistentList())),
            aMediaGalleryState(
                imageItems = AsyncData.Success(
                    listOf(
                        aDate(),
                        anImage(),
                        aDate(),
                        anImage(),
                        aVideo(),
                        anImage(),
                        anImage(),
                        anImage(),
                        anImage(),
                        anImage(),
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
                        aDate(),
                        aFile(),
                        aDate(),
                        aFile(),
                        aFile(),
                        aFile(),
                        aFile(),
                    ).toImmutableList()
                )
            ),
        )
}

private fun aMediaGalleryState(
    roomName: String = "Room name",
    mode: MediaGalleryMode = MediaGalleryMode.Images,
    imageItems: AsyncData<ImmutableList<MediaItem>> = AsyncData.Uninitialized,
    fileItems: AsyncData<ImmutableList<MediaItem>> = AsyncData.Uninitialized,
) = MediaGalleryState(
    roomName = roomName,
    mode = mode,
    imageItems = imageItems,
    fileItems = fileItems,
    mediaBottomSheetState = MediaBottomSheetState.Hidden,
    snackbarMessage = null,
    eventSink = {}
)
