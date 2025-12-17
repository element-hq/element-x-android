/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.model

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.mediaviewer.api.MediaInfo

sealed interface MediaItem {
    data class DateSeparator(
        val id: UniqueId,
        val formattedDate: String,
    ) : MediaItem

    data class LoadingIndicator(
        val id: UniqueId,
        val direction: Timeline.PaginationDirection,
        val timestamp: Long,
    ) : MediaItem

    sealed interface Event : MediaItem

    data class Image(
        val id: UniqueId,
        val eventId: EventId?,
        val mediaInfo: MediaInfo,
        val mediaSource: MediaSource,
        val thumbnailSource: MediaSource?,
    ) : Event {
        val thumbnailMediaRequestData: MediaRequestData
            get() = MediaRequestData(thumbnailSource ?: mediaSource, MediaRequestData.Kind.Thumbnail(100))
    }

    data class Video(
        val id: UniqueId,
        val eventId: EventId?,
        val mediaInfo: MediaInfo,
        val mediaSource: MediaSource,
        val thumbnailSource: MediaSource?,
    ) : Event {
        val thumbnailMediaRequestData: MediaRequestData
            get() = MediaRequestData(thumbnailSource ?: mediaSource, MediaRequestData.Kind.Thumbnail(100))
    }

    data class Audio(
        val id: UniqueId,
        val eventId: EventId?,
        val mediaInfo: MediaInfo,
        val mediaSource: MediaSource,
    ) : Event

    data class Voice(
        val id: UniqueId,
        val eventId: EventId?,
        val mediaInfo: MediaInfo,
        val mediaSource: MediaSource,
    ) : Event

    data class File(
        val id: UniqueId,
        val eventId: EventId?,
        val mediaInfo: MediaInfo,
        val mediaSource: MediaSource,
    ) : Event
}

fun MediaItem.id(): UniqueId {
    return when (this) {
        is MediaItem.DateSeparator -> id
        is MediaItem.LoadingIndicator -> id
        is MediaItem.Image -> id
        is MediaItem.Video -> id
        is MediaItem.File -> id
        is MediaItem.Audio -> id
        is MediaItem.Voice -> id
    }
}

fun MediaItem.Event.eventId(): EventId? {
    return when (this) {
        is MediaItem.Image -> eventId
        is MediaItem.Video -> eventId
        is MediaItem.File -> eventId
        is MediaItem.Audio -> eventId
        is MediaItem.Voice -> eventId
    }
}

fun MediaItem.Event.mediaInfo(): MediaInfo {
    return when (this) {
        is MediaItem.Image -> mediaInfo
        is MediaItem.Video -> mediaInfo
        is MediaItem.File -> mediaInfo
        is MediaItem.Audio -> mediaInfo
        is MediaItem.Voice -> mediaInfo
    }
}

fun MediaItem.Event.mediaSource(): MediaSource {
    return when (this) {
        is MediaItem.Image -> mediaSource
        is MediaItem.Video -> mediaSource
        is MediaItem.File -> mediaSource
        is MediaItem.Audio -> mediaSource
        is MediaItem.Voice -> mediaSource
    }
}

fun MediaItem.Event.thumbnailSource(): MediaSource? {
    return when (this) {
        is MediaItem.Image -> thumbnailSource
        is MediaItem.Video -> thumbnailSource
        is MediaItem.File -> null
        is MediaItem.Audio -> null
        is MediaItem.Voice -> null
    }
}
