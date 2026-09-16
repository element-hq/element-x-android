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

sealed interface MediaGalleryEvent {
    data class ChangeMode(val mode: MediaGalleryMode) : MediaGalleryEvent
    data class LoadMore(val direction: Timeline.PaginationDirection) : MediaGalleryEvent
    data class Share(val eventId: EventId) : MediaGalleryEvent
    data class Forward(val eventId: EventId) : MediaGalleryEvent
    data class SaveOnDisk(val eventId: EventId) : MediaGalleryEvent
    data class OpenWith(val eventId: EventId) : MediaGalleryEvent
    data class OpenInfo(val mediaItem: MediaItem.Event) : MediaGalleryEvent
    data class ViewInTimeline(val eventId: EventId) : MediaGalleryEvent

    data class ConfirmDelete(
        val eventId: EventId,
        val mediaInfo: MediaInfo,
        val thumbnailSource: MediaSource?,
    ) : MediaGalleryEvent

    data object CloseBottomSheet : MediaGalleryEvent
    data class Delete(val eventId: EventId) : MediaGalleryEvent
}
