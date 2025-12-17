/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.impl.model.MediaItem

sealed interface MediaGalleryEvents {
    data class ChangeMode(val mode: MediaGalleryMode) : MediaGalleryEvents
    data class LoadMore(val direction: Timeline.PaginationDirection) : MediaGalleryEvents
    data class Share(val eventId: EventId) : MediaGalleryEvents
    data class Forward(val eventId: EventId) : MediaGalleryEvents
    data class SaveOnDisk(val eventId: EventId) : MediaGalleryEvents
    data class OpenInfo(val mediaItem: MediaItem.Event) : MediaGalleryEvents
    data class ViewInTimeline(val eventId: EventId) : MediaGalleryEvents

    data class ConfirmDelete(
        val eventId: EventId,
        val mediaInfo: MediaInfo,
        val thumbnailSource: MediaSource?,
    ) : MediaGalleryEvents

    data object CloseBottomSheet : MediaGalleryEvents
    data class Delete(val eventId: EventId) : MediaGalleryEvents
}
