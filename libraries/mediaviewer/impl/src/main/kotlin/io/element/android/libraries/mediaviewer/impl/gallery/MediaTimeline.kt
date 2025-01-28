/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import kotlinx.collections.immutable.persistentListOf
import javax.inject.Inject

interface MediaTimeline {
    suspend fun getTimeline(): Result<Timeline>
    val cache: GroupedMediaItems?
    fun orCache(data: GroupedMediaItems): GroupedMediaItems
}

/**
 * A timeline holder that can be used by the gallery and the media viewer.
 * When opening the Media Viewer, if the held timeline knows the Event, it will
 * be used, else a FocusedMediaTimeline will be used.
 */
@SingleIn(RoomScope::class)
@ContributesBinding(RoomScope::class)
class LiveMediaTimeline @Inject constructor(
    private val room: MatrixRoom,
) : MediaTimeline {
    private var timeline: Timeline? = null
    override suspend fun getTimeline(): Result<Timeline> {
        return if (timeline == null) {
            room.mediaTimeline(null).fold(
                {
                    timeline = it
                    Result.success(it)
                },
                {
                    Result.failure(it)
                },
            )
        } else {
            Result.success(timeline!!)
        }
    }

    // No cache for LiveMediaTimeline
    override val cache = null
    override fun orCache(data: GroupedMediaItems) = data
}

/**
 * A class that will provide a media timeline that is focused on a particular event.
 */
class FocusedMediaTimeline(
    private val room: MatrixRoom,
    private val eventId: EventId,
    initialMediaItem: MediaItem.Event,
) : MediaTimeline {
    override suspend fun getTimeline(): Result<Timeline> {
        return room.mediaTimeline(eventId)
    }

    override val cache = persistentListOf(
        MediaItem.LoadingIndicator(
            id = UniqueId("loading_forwards"),
            direction = Timeline.PaginationDirection.FORWARDS,
            timestamp = 0L,
        ),
        initialMediaItem,
        MediaItem.LoadingIndicator(
            id = UniqueId("loading_backwards"),
            direction = Timeline.PaginationDirection.BACKWARDS,
            timestamp = 0L,
        ),
    ).let {
        GroupedMediaItems(
            fileItems = it,
            imageAndVideoItems = it,
        )
    }

    override fun orCache(data: GroupedMediaItems): GroupedMediaItems {
        return if (data.hasEvent(eventId)) {
            data
        } else {
            cache
        }
    }
}
