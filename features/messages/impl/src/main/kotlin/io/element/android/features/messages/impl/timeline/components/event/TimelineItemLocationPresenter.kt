/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import io.element.android.features.location.api.live.ActiveLiveLocationShareManager
import io.element.android.features.messages.impl.timeline.di.TimelineItemEventContentKey
import io.element.android.features.messages.impl.timeline.di.TimelineItemPresenterFactory
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.ensureActiveLiveLocation
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.room.JoinedRoom
import kotlinx.coroutines.flow.map

@BindingContainer
@ContributesTo(RoomScope::class)
interface TimelineItemLocationPresenterModule {
    @Binds
    @IntoMap
    @TimelineItemEventContentKey(TimelineItemLocationContent::class)
    fun bindTimelineItemLocationPresenterFactory(factory: TimelineItemLocationPresenter.Factory): TimelineItemPresenterFactory<*, *>
}

@AssistedInject
class TimelineItemLocationPresenter(
    private val room: JoinedRoom,
    private val liveLocationShareManager: ActiveLiveLocationShareManager,
    @Assisted private val content: TimelineItemLocationContent,
) : Presenter<TimelineItemLocationContent> {
    @AssistedFactory
    fun interface Factory : TimelineItemPresenterFactory<TimelineItemLocationContent, TimelineItemLocationContent> {
        override fun create(content: TimelineItemLocationContent): TimelineItemLocationPresenter
    }

    @Composable
    override fun present(): TimelineItemLocationContent {
        val content = content.ensureActiveLiveLocation()
        val hasActiveShare by remember {
            liveLocationShareManager.activeShares
                .map { shares ->
                    shares[room.roomId]?.sessionId == content.senderId
                }
        }.collectAsState(initial = false)

        return when (val mode = content.mode) {
            is TimelineItemLocationContent.Mode.Live -> content.copy(
                mode = mode.copy(canStop = mode.isActive && hasActiveShare)
            )
            is TimelineItemLocationContent.Mode.Static -> content
        }
    }
}
