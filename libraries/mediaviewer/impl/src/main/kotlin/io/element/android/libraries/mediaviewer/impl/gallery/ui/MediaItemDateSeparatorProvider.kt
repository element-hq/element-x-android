/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.mediaviewer.impl.gallery.MediaItem

class MediaItemDateSeparatorProvider : PreviewParameterProvider<MediaItem.DateSeparator> {
    override val values: Sequence<MediaItem.DateSeparator>
        get() = sequenceOf(
            aMediaItemDateSeparator(),
            aMediaItemDateSeparator(formattedDate = "A long date that should be truncated"),
        )
}

fun aMediaItemDateSeparator(
    id: UniqueId = UniqueId("dateId"),
    formattedDate: String = "October 2024",
): MediaItem.DateSeparator {
    return MediaItem.DateSeparator(
        id = id,
        formattedDate = formattedDate,
    )
}
