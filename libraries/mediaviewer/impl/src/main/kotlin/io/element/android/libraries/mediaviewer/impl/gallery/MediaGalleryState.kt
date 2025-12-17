/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.mediaviewer.impl.R
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import io.element.android.libraries.mediaviewer.impl.model.GroupedMediaItems

data class MediaGalleryState(
    val roomName: String,
    val mode: MediaGalleryMode,
    val groupedMediaItems: AsyncData<GroupedMediaItems>,
    val mediaBottomSheetState: MediaBottomSheetState,
    val snackbarMessage: SnackbarMessage?,
    val eventSink: (MediaGalleryEvents) -> Unit,
)

enum class MediaGalleryMode(val stringResource: Int) {
    Images(R.string.screen_media_browser_list_mode_media),
    Files(R.string.screen_media_browser_list_mode_files),
}
