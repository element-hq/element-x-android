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
import androidx.media3.common.MimeTypes
import io.element.android.features.messages.impl.media.local.LocalMedia
import io.element.android.libraries.architecture.Async

open class MediaViewerStateProvider : PreviewParameterProvider<MediaViewerState> {
    override val values: Sequence<MediaViewerState>
        get() = sequenceOf(
            aMediaViewerState(),
            aMediaViewerState(Async.Loading()),
            aMediaViewerState(Async.Failure(IllegalStateException())),
            aMediaViewerState(
                Async.Success(
                    LocalMedia(
                        Uri.EMPTY, MimeTypes.IMAGE_JPEG, "an image file", 100L
                    )
                ),
            ),
            aMediaViewerState(
                Async.Success(
                    LocalMedia(
                        Uri.EMPTY, MimeTypes.VIDEO_MP4, "a video file", 100L
                    )
                ),
            )
        )
}

fun aMediaViewerState(downloadedMedia: Async<LocalMedia> = Async.Uninitialized) = MediaViewerState(
    name = "A media",
    mimeType = MimeTypes.IMAGE_JPEG,
    thumbnailSource = null,
    downloadedMedia = downloadedMedia,
) {}
