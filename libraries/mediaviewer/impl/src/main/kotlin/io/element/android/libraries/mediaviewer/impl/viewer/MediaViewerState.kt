/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState

data class MediaViewerState(
    val eventId: EventId?,
    val mediaInfo: MediaInfo,
    val thumbnailSource: MediaSource?,
    val downloadedMedia: AsyncData<LocalMedia>,
    val snackbarMessage: SnackbarMessage?,
    val canShowInfo: Boolean,
    val mediaBottomSheetState: MediaBottomSheetState,
    val eventSink: (MediaViewerEvents) -> Unit,
)
