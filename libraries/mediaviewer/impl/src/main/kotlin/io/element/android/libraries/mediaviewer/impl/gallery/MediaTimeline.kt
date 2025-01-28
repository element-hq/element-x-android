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
    fun getCache(): GroupedMediaItems?
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
    override fun getCache(): GroupedMediaItems? = null
    override fun orCache(data: GroupedMediaItems) = data
}

/**
 * A class that will provide a media timeline that is focused on a particular event.
 */
class FocusedMediaTimeline(
    private val room: MatrixRoom,
    private val eventId: EventId,
    private val initialMediaItem: MediaItem.Event,
) : MediaTimeline {
    override suspend fun getTimeline(): Result<Timeline> {
        return room.mediaTimeline(eventId)
    }

    override fun getCache(): GroupedMediaItems {
        // TODO Cleanup
        return GroupedMediaItems(
            fileItems = persistentListOf(
                MediaItem.LoadingIndicator(
                    id = UniqueId("loading_forwards"),
                    direction = Timeline.PaginationDirection.FORWARDS,
                    timestamp = -1L,
                ),
                initialMediaItem,
                MediaItem.LoadingIndicator(
                    id = UniqueId("loading_backwards"),
                    direction = Timeline.PaginationDirection.BACKWARDS,
                    timestamp = -1L,
                ),
            ),
            imageAndVideoItems = persistentListOf(
                MediaItem.LoadingIndicator(
                    id = UniqueId("loading_forwards"),
                    direction = Timeline.PaginationDirection.FORWARDS,
                    timestamp = -1L,
                ),
                initialMediaItem,
                MediaItem.LoadingIndicator(
                    id = UniqueId("loading_backwards"),
                    direction = Timeline.PaginationDirection.BACKWARDS,
                    timestamp = -1L,
                ),
            ),
        )
    }

    override fun orCache(data: GroupedMediaItems): GroupedMediaItems {
        return if (data.hasEvent(eventId)) {
            data
        } else {
            getCache()
        }
    }
}

fun GroupedMediaItems.hasEvent(eventId: EventId): Boolean {
    return (fileItems + imageAndVideoItems)
        .filterIsInstance<MediaItem.Event>()
        .any { it.eventId() == eventId }
}
