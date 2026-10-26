/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.element.android.features.messages.impl.timeline.TimelineController
import io.element.android.features.messages.impl.timeline.di.LiveTimeline
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.RoomCoroutineScope
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.TimelineProvider
import kotlinx.coroutines.CoroutineScope

@ContributesTo(RoomScope::class)
@BindingContainer
object MessagesProvidesBindingContainer {
    @Provides
    @LiveTimeline
    fun providesLiveTimeline(joinedRoom: JoinedRoom): Timeline = joinedRoom.liveTimeline

    @Provides
    @SingleIn(RoomScope::class)
    fun providesTimelineController(
        joinedRoom: JoinedRoom,
        @LiveTimeline liveTimeline: Timeline,
        @RoomCoroutineScope roomCoroutineScope: CoroutineScope,
        dispatchers: CoroutineDispatchers,
    ): TimelineController = TimelineController(joinedRoom, liveTimeline, roomCoroutineScope, dispatchers)

    @Provides
    @SingleIn(RoomScope::class)
    fun providesTimelineProvider(timelineController: TimelineController): TimelineProvider = timelineController
}
