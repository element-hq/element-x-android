/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.viewer

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.MediaInfo

data class MediaViewerState(
    val mediaInfo: MediaInfo,
    val thumbnailSource: MediaSource?,
    val downloadedMedia: AsyncData<LocalMedia>,
    val snackbarMessage: SnackbarMessage?,
    val canDownload: Boolean,
    val canShare: Boolean,
    val eventSink: (MediaViewerEvents) -> Unit,
)
