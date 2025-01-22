/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaviewer.impl.gallery.MediaItem

fun aMediaItemLoadingIndicator(
    id: UniqueId = UniqueId("loadingId"),
    direction: Timeline.PaginationDirection = Timeline.PaginationDirection.BACKWARDS,
): MediaItem.LoadingIndicator {
    return MediaItem.LoadingIndicator(
        id = id,
        direction = direction,
        timestamp = 123,
    )
}
