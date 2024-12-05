/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediagallery.impl.gallery

import io.element.android.libraries.matrix.api.timeline.Timeline

sealed interface MediaGalleryEvents {
    data class ChangeMode(val mode: MediaGalleryMode) : MediaGalleryEvents
    data class LoadMore(val direction: Timeline.PaginationDirection) : MediaGalleryEvents
}
