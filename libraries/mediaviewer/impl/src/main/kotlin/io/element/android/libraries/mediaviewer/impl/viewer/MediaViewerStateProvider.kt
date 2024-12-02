/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import android.net.Uri
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.aPdfMediaInfo
import io.element.android.libraries.mediaviewer.api.aVideoMediaInfo
import io.element.android.libraries.mediaviewer.api.anApkMediaInfo
import io.element.android.libraries.mediaviewer.api.anAudioMediaInfo
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia

open class MediaViewerStateProvider : PreviewParameterProvider<MediaViewerState> {
    override val values: Sequence<MediaViewerState>
        get() = sequenceOf(
            aMediaViewerState(),
            aMediaViewerState(AsyncData.Loading()),
            aMediaViewerState(AsyncData.Failure(IllegalStateException("error"))),
            aMediaViewerState(
                AsyncData.Success(
                    LocalMedia(Uri.EMPTY, anImageMediaInfo())
                ),
                anImageMediaInfo(),
            ),
            aMediaViewerState(
                AsyncData.Success(
                    LocalMedia(Uri.EMPTY, aVideoMediaInfo())
                ),
                aVideoMediaInfo(),
            ),
            aMediaViewerState(
                AsyncData.Success(
                    LocalMedia(Uri.EMPTY, aPdfMediaInfo())
                ),
                aPdfMediaInfo(),
            ),
            aMediaViewerState(
                AsyncData.Loading(),
                anApkMediaInfo(),
            ),
            aMediaViewerState(
                AsyncData.Success(
                    LocalMedia(Uri.EMPTY, anApkMediaInfo())
                ),
                anApkMediaInfo(),
            ),
            aMediaViewerState(
                AsyncData.Loading(),
                anAudioMediaInfo(),
            ),
            aMediaViewerState(
                AsyncData.Success(
                    LocalMedia(Uri.EMPTY, anAudioMediaInfo())
                ),
                anAudioMediaInfo(),
            ),
            aMediaViewerState(
                AsyncData.Success(
                    LocalMedia(Uri.EMPTY, anImageMediaInfo())
                ),
                anImageMediaInfo(),
                canDownload = false,
                canShare = false,
            ),
        )
}

fun aMediaViewerState(
    downloadedMedia: AsyncData<LocalMedia> = AsyncData.Uninitialized,
    mediaInfo: MediaInfo = anImageMediaInfo(),
    canDownload: Boolean = true,
    canShare: Boolean = true,
    eventSink: (MediaViewerEvents) -> Unit = {},
) = MediaViewerState(
    mediaInfo = mediaInfo,
    thumbnailSource = null,
    downloadedMedia = downloadedMedia,
    snackbarMessage = null,
    canDownload = canDownload,
    canShare = canShare,
    eventSink = eventSink,
)
