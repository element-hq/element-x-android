/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.fileviewer

import android.net.Uri
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia

private const val A_FILENAME = "push_rules@alice_server.org.json"

open class FileViewerStatePreviewParam : PreviewParameterProvider<FileViewerState> {
    override val values: Sequence<FileViewerState>
        get() = sequenceOf(
            aFileViewerState(),
            aFileViewerState(localMedia = AsyncData.Failure(Exception("A failure"))),
            aFileViewerState(
                localMedia = AsyncData.Success(
                    LocalMedia(
                        uri = Uri.EMPTY,
                        info = aJsonMediaInfo(),
                    )
                )
            ),
        )
}

fun aFileViewerState(
    filename: String = A_FILENAME,
    localMedia: AsyncData<LocalMedia> = AsyncData.Uninitialized,
    eventSink: (FileViewerEvent) -> Unit = {},
) = FileViewerState(
    filename = filename,
    localMedia = localMedia,
    snackbarMessage = null,
    eventSink = eventSink,
)

fun aJsonMediaInfo(
    filename: String = A_FILENAME,
) = MediaInfo(
    filename = filename,
    caption = null,
    mimeType = MimeTypes.Json,
    fileSize = 2 * 1024,
    formattedFileSize = "2kB",
    fileExtension = "json",
    senderId = null,
    senderName = null,
    senderAvatar = null,
    dateSent = null,
    dateSentFull = null,
    waveform = null,
    duration = null,
)
