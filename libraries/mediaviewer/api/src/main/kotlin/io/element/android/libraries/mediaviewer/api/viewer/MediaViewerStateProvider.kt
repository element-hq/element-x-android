/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.mediaviewer.api.viewer

import android.net.Uri
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.MediaInfo
import io.element.android.libraries.mediaviewer.api.local.aFileInfo
import io.element.android.libraries.mediaviewer.api.local.aPdfInfo
import io.element.android.libraries.mediaviewer.api.local.aVideoInfo
import io.element.android.libraries.mediaviewer.api.local.anAudioInfo
import io.element.android.libraries.mediaviewer.api.local.anImageInfo

open class MediaViewerStateProvider : PreviewParameterProvider<MediaViewerState> {
    override val values: Sequence<MediaViewerState>
        get() = sequenceOf(
            aMediaViewerState(),
            aMediaViewerState(AsyncData.Loading()),
            aMediaViewerState(AsyncData.Failure(IllegalStateException("error"))),
            aMediaViewerState(
                AsyncData.Success(
                    LocalMedia(Uri.EMPTY, anImageInfo())
                ),
                anImageInfo(),
            ),
            aMediaViewerState(
                AsyncData.Success(
                    LocalMedia(Uri.EMPTY, aVideoInfo())
                ),
                aVideoInfo(),
            ),
            aMediaViewerState(
                AsyncData.Success(
                    LocalMedia(Uri.EMPTY, aPdfInfo())
                ),
                aPdfInfo(),
            ),
            aMediaViewerState(
                AsyncData.Loading(),
                aFileInfo(),
            ),
            aMediaViewerState(
                AsyncData.Success(
                    LocalMedia(Uri.EMPTY, aFileInfo())
                ),
                aFileInfo(),
            ),
            aMediaViewerState(
                AsyncData.Loading(),
                anAudioInfo(),
            ),
            aMediaViewerState(
                AsyncData.Success(
                    LocalMedia(Uri.EMPTY, anAudioInfo())
                ),
                anAudioInfo(),
            ),
            aMediaViewerState(
                AsyncData.Success(
                    LocalMedia(Uri.EMPTY, anImageInfo())
                ),
                anImageInfo(),
                canDownload = false,
                canShare = false,
            ),
        )
}

fun aMediaViewerState(
    downloadedMedia: AsyncData<LocalMedia> = AsyncData.Uninitialized,
    mediaInfo: MediaInfo = anImageInfo(),
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
