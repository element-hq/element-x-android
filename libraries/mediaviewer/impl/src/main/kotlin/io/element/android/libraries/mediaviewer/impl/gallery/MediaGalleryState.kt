/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.mediaviewer.impl.R
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import kotlinx.collections.immutable.ImmutableList

data class MediaGalleryState(
    val roomName: String,
    val mode: MediaGalleryMode,
    val groupedMediaItems: AsyncData<GroupedMediaItems>,
    val mediaBottomSheetState: MediaBottomSheetState,
    val snackbarMessage: SnackbarMessage?,
    val eventSink: (MediaGalleryEvents) -> Unit,
)

data class GroupedMediaItems(
    val imageAndVideoItems: ImmutableList<MediaItem>,
    val fileItems: ImmutableList<MediaItem>,
)

enum class MediaGalleryMode(val stringResource: Int) {
    Images(R.string.screen_media_browser_list_mode_media),
    Files(R.string.screen_media_browser_list_mode_files),
}
