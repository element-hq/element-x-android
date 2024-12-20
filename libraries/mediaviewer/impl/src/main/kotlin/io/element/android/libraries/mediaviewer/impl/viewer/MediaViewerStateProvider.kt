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
import io.element.android.libraries.designsystem.components.media.aWaveForm
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.aPdfMediaInfo
import io.element.android.libraries.mediaviewer.api.aVideoMediaInfo
import io.element.android.libraries.mediaviewer.api.anApkMediaInfo
import io.element.android.libraries.mediaviewer.api.anAudioMediaInfo
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import io.element.android.libraries.mediaviewer.impl.details.aMediaDeleteConfirmationState
import io.element.android.libraries.mediaviewer.impl.details.aMediaDetailsBottomSheetState

open class MediaViewerStateProvider : PreviewParameterProvider<MediaViewerState> {
    override val values: Sequence<MediaViewerState>
        get() = sequenceOf(
            aMediaViewerState(),
            aMediaViewerState(AsyncData.Loading()),
            aMediaViewerState(AsyncData.Failure(IllegalStateException("error"))),
            anImageMediaInfo(
                senderName = "Sally Sanderson",
                dateSent = "21 NOV, 2024",
                caption = "A caption",
            ).let {
                aMediaViewerState(
                    downloadedMedia = AsyncData.Success(
                        LocalMedia(Uri.EMPTY, it)
                    ),
                    mediaInfo = it,
                )
            },
            aVideoMediaInfo(
                senderName = "A very long name so that it will be truncated and will not be displayed on multiple lines",
                dateSent = "A very very long date that will be truncated and will not be displayed on multiple lines",
                caption = "A caption",
            ).let {
                aMediaViewerState(
                    downloadedMedia = AsyncData.Success(
                        LocalMedia(Uri.EMPTY, it)
                    ),
                    mediaInfo = it,
                )
            },
            aPdfMediaInfo().let {
                aMediaViewerState(
                    downloadedMedia = AsyncData.Success(
                        LocalMedia(Uri.EMPTY, it)
                    ),
                    mediaInfo = it,
                )
            },
            aMediaViewerState(
                downloadedMedia = AsyncData.Loading(),
                mediaInfo = anApkMediaInfo(),
            ),
            anApkMediaInfo().let {
                aMediaViewerState(
                    downloadedMedia = AsyncData.Success(
                        LocalMedia(Uri.EMPTY, it)
                    ),
                    mediaInfo = it,
                )
            },
            aMediaViewerState(
                downloadedMedia = AsyncData.Loading(),
                mediaInfo = anAudioMediaInfo(),
            ),
            anAudioMediaInfo().let {
                aMediaViewerState(
                    downloadedMedia = AsyncData.Success(
                        LocalMedia(Uri.EMPTY, it)
                    ),
                    mediaInfo = it,
                )
            },
            anImageMediaInfo().let {
                aMediaViewerState(
                    downloadedMedia = AsyncData.Success(
                        LocalMedia(Uri.EMPTY, it)
                    ),
                    mediaInfo = it,
                    canShowInfo = false,
                )
            },
            aMediaViewerState(
                mediaBottomSheetState = aMediaDetailsBottomSheetState(),
            ),
            aMediaViewerState(
                mediaBottomSheetState = aMediaDeleteConfirmationState(),
            ),
            anAudioMediaInfo(
                waveForm = aWaveForm(),
            ).let {
                aMediaViewerState(
                    downloadedMedia = AsyncData.Success(
                        LocalMedia(Uri.EMPTY, it)
                    ),
                    mediaInfo = it,
                )
            },
        )
}

fun aMediaViewerState(
    downloadedMedia: AsyncData<LocalMedia> = AsyncData.Uninitialized,
    mediaInfo: MediaInfo = anImageMediaInfo(),
    canShowInfo: Boolean = true,
    mediaBottomSheetState: MediaBottomSheetState = MediaBottomSheetState.Hidden,
    eventSink: (MediaViewerEvents) -> Unit = {},
) = MediaViewerState(
    eventId = null,
    mediaInfo = mediaInfo,
    thumbnailSource = null,
    downloadedMedia = downloadedMedia,
    snackbarMessage = null,
    canShowInfo = canShowInfo,
    mediaBottomSheetState = mediaBottomSheetState,
    eventSink = eventSink,
)
