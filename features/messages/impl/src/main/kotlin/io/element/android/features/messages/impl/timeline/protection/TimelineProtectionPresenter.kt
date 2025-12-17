/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaPreviewService
import io.element.android.libraries.matrix.api.media.isPreviewEnabled
import io.element.android.libraries.matrix.api.room.BaseRoom
import kotlinx.collections.immutable.toImmutableSet

@Inject
class TimelineProtectionPresenter(
    private val mediaPreviewService: MediaPreviewService,
    private val room: BaseRoom,
) : Presenter<TimelineProtectionState> {
    private val allowedEvents = mutableStateOf<Set<EventId>>(setOf())

    @Composable
    override fun present(): TimelineProtectionState {
        val mediaPreviewValue = remember {
            mediaPreviewService.mediaPreviewConfigFlow.mapState { config -> config.mediaPreviewValue }
        }.collectAsState()
        val roomInfo = room.roomInfoFlow.collectAsState()
        val protectionState by remember {
            derivedStateOf {
                val isPreviewEnabled = mediaPreviewValue.value.isPreviewEnabled(roomInfo.value.joinRule)
                if (isPreviewEnabled) {
                    ProtectionState.RenderAll
                } else {
                    ProtectionState.RenderOnly(eventIds = allowedEvents.value.toImmutableSet())
                }
            }
        }

        fun handleEvent(event: TimelineProtectionEvent) {
            when (event) {
                is TimelineProtectionEvent.ShowContent -> {
                    allowedEvents.value = allowedEvents.value + setOfNotNull(event.eventId)
                }
            }
        }

        return TimelineProtectionState(
            protectionState = protectionState,
            eventSink = ::handleEvent,
        )
    }
}
