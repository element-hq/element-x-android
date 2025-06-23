/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.matrix.api.media.isPreviewEnabled
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.collections.immutable.toImmutableSet
import javax.inject.Inject

class TimelineProtectionPresenter @Inject constructor(
    private val appPreferencesStore: AppPreferencesStore,
    private val room: BaseRoom,
) : Presenter<TimelineProtectionState> {
    private val allowedEvents = mutableStateOf<Set<EventId>>(setOf())

    @Composable
    override fun present(): TimelineProtectionState {
        val mediaPreviewValue = remember {
            appPreferencesStore.getTimelineMediaPreviewValueFlow()
        }.collectAsState(initial = MediaPreviewValue.On)
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
            eventSink = { event -> handleEvent(event) }
        )
    }
}
