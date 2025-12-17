/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.datasource

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaviewer.impl.model.GroupedMediaItems
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.hasEvent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
class LiveMediaTimeline(
    private val room: JoinedRoom,
) : MediaTimeline {
    private var timeline: Timeline? = null
    private val mutex = Mutex()

    override suspend fun getTimeline(): Result<Timeline> = mutex.withLock {
        val currentTimeline = timeline
        if (currentTimeline == null) {
            room.createTimeline(CreateTimelineParams.MediaOnly)
                .onSuccess { timeline = it }
        } else {
            Result.success(currentTimeline)
        }
    }

    // No cache for LiveMediaTimeline
    override val cache = null
    override fun orCache(data: GroupedMediaItems) = data
}

/**
 * A class that will provide a media timeline that is focused on a particular event.
 * Optionally, the timeline will only contain the pinned events.
 */
class FocusedMediaTimeline(
    private val room: JoinedRoom,
    private val eventId: EventId,
    private val onlyPinnedEvents: Boolean,
    initialMediaItem: MediaItem.Event,
) : MediaTimeline {
    override suspend fun getTimeline(): Result<Timeline> {
        return room.createTimeline(
            createTimelineParams = if (onlyPinnedEvents) {
                CreateTimelineParams.PinnedOnly
            } else {
                CreateTimelineParams.MediaOnlyFocused(eventId)
            },
        )
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
