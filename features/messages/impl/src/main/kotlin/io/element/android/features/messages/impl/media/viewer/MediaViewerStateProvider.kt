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

package io.element.android.features.messages.impl.media.viewer

import android.net.Uri
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.media.local.LocalMedia
import io.element.android.features.messages.impl.media.local.MediaInfo
import io.element.android.features.messages.impl.media.local.aFileInfo
import io.element.android.features.messages.impl.media.local.aPdfInfo
import io.element.android.features.messages.impl.media.local.aVideoInfo
import io.element.android.features.messages.impl.media.local.anImageInfo
import io.element.android.libraries.architecture.Async

open class MediaViewerStateProvider : PreviewParameterProvider<MediaViewerState> {
    override val values: Sequence<MediaViewerState>
        get() = sequenceOf(
            aMediaViewerState(),
            aMediaViewerState(Async.Loading()),
            aMediaViewerState(Async.Failure(IllegalStateException())),
            aMediaViewerState(
                Async.Success(
                    LocalMedia(Uri.EMPTY, anImageInfo())
                ),
                anImageInfo(),
            ),
            aMediaViewerState(
                Async.Success(
                    LocalMedia(Uri.EMPTY, aVideoInfo())
                ),
                aVideoInfo(),
            ),
            aMediaViewerState(
                Async.Success(
                    LocalMedia(Uri.EMPTY, aPdfInfo())
                ),
                aPdfInfo(),
            ),
            aMediaViewerState(
                Async.Success(
                    LocalMedia(Uri.EMPTY, aFileInfo())
                ),
                aFileInfo(),
            )
        )
}

fun aMediaViewerState(
    downloadedMedia: Async<LocalMedia> = Async.Uninitialized,
    mediaInfo: MediaInfo = anImageInfo(),
) = MediaViewerState(
    mediaInfo = mediaInfo,
    thumbnailSource = null,
    downloadedMedia = downloadedMedia,
    snackbarMessage = null
) {}
