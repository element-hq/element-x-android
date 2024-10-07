/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.collections.immutable.toImmutableSet
import javax.inject.Inject

class TimelineProtectionPresenter @Inject constructor(
    private val appPreferencesStore: AppPreferencesStore,
) : Presenter<TimelineProtectionState> {
    @Composable
    override fun present(): TimelineProtectionState {
        val hideMediaContent by appPreferencesStore.doesHideImagesAndVideosFlow().collectAsState(initial = false)
        var allowedEvents by remember { mutableStateOf<Set<EventId>>(setOf()) }
        val protectionState by remember(hideMediaContent) {
            derivedStateOf {
                if (hideMediaContent) {
                    ProtectionState.RenderOnly(eventIds = allowedEvents.toImmutableSet())
                } else {
                    ProtectionState.RenderAll
                }
            }
        }

        fun handleEvent(event: TimelineProtectionEvent) {
            when (event) {
                is TimelineProtectionEvent.ShowContent -> {
                    allowedEvents = allowedEvents + setOfNotNull(event.eventId)
                }
            }
        }

        return TimelineProtectionState(
            protectionState = protectionState,
            eventSink = { event -> handleEvent(event) }
        )
    }
}
