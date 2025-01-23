/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo
import io.element.android.libraries.mediaviewer.impl.gallery.MediaItem

fun aMediaItemImage(
    id: UniqueId = UniqueId("imageId"),
    eventId: EventId? = null,
    senderId: UserId? = null,
    mediaSourceUrl: String = "",
): MediaItem.Image {
    return MediaItem.Image(
        id = id,
        eventId = eventId,
        mediaInfo = anImageMediaInfo(
            senderId = senderId,
        ),
        mediaSource = MediaSource(mediaSourceUrl),
        thumbnailSource = null,
    )
}
