/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.core.preview.loremIpsum
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.aPdfMediaInfo
import io.element.android.libraries.mediaviewer.impl.gallery.MediaItem

class MediaItemFileProvider : PreviewParameterProvider<MediaItem.File> {
    override val values: Sequence<MediaItem.File>
        get() = sequenceOf(
            aMediaItemFile(),
            aMediaItemFile(
                filename = "A long filename that should be truncated.jpg",
                caption = "A caption",
            ),
            aMediaItemFile(
                caption = loremIpsum,
            ),
        )
}

fun aMediaItemFile(
    id: UniqueId = UniqueId("fileId"),
    eventId: EventId? = null,
    filename: String = "filename",
    caption: String? = null,
): MediaItem.File {
    return MediaItem.File(
        id = id,
        eventId = eventId,
        mediaInfo = aPdfMediaInfo(
            filename = filename,
            caption = caption,
        ),
        mediaSource = MediaSource(""),
    )
}
