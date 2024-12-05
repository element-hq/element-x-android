/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediagallery.impl.gallery.ui

import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediagallery.impl.gallery.MediaItem
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo

fun anImage(): MediaItem.Image {
    return MediaItem.Image(
        id = UniqueId("imageId"),
        eventId = null,
        mediaInfo = anImageMediaInfo(),
        mediaSource = MediaSource(""),
        thumbnailSource = null,
    )
}
