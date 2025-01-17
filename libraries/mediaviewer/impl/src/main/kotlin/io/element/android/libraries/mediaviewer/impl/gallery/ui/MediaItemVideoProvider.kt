/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.aVideoMediaInfo
import io.element.android.libraries.mediaviewer.impl.gallery.MediaItem

class MediaItemVideoProvider : PreviewParameterProvider<MediaItem.Video> {
    override val values: Sequence<MediaItem.Video>
        get() = sequenceOf(
            aMediaItemVideo(),
            aMediaItemVideo(
                duration = null,
            ),
        )
}

fun aMediaItemVideo(
    id: UniqueId = UniqueId("videoId"),
    mediaSource: MediaSource = MediaSource(""),
    duration: String? = "1:23",
): MediaItem.Video {
    return MediaItem.Video(
        id = id,
        eventId = null,
        mediaInfo = aVideoMediaInfo(
            duration = duration
        ),
        mediaSource = mediaSource,
        thumbnailSource = null,
    )
}
