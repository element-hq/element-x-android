/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned

import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.TimelineProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@SingleIn(RoomScope::class)
class PinnedEventsTimelineProvider @Inject constructor(
    private val room: MatrixRoom,
    private val networkMonitor: NetworkMonitor,
    private val featureFlagService: FeatureFlagService,
) : TimelineProvider {
    private val _timelineStateFlow: MutableStateFlow<AsyncData<Timeline>> = MutableStateFlow(AsyncData.Uninitialized)

    override fun activeTimelineFlow(): StateFlow<Timeline?> {
        return _timelineStateFlow
            .mapState { value ->
                value.dataOrNull()
            }
    }

    val timelineStateFlow = _timelineStateFlow

    fun launchIn(scope: CoroutineScope) {
        combine(
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.PinnedEvents),
            networkMonitor.connectivity
        ) {
          // do not use connectivity here as data can be loaded from cache, it's just to trigger retry if needed
          isEnabled, _ ->
            isEnabled
        }
            .onEach { isFeatureEnabled ->
                if (isFeatureEnabled) {
                    loadTimelineIfNeeded()
                } else {
                    _timelineStateFlow.value = AsyncData.Uninitialized
                }
            }
            .onCompletion {
                invokeOnTimeline { close() }
            }
            .launchIn(scope)
    }

    suspend fun invokeOnTimeline(action: suspend Timeline.() -> Unit) {
        when (val asyncTimeline = timelineStateFlow.value) {
            is AsyncData.Success -> action(asyncTimeline.data)
            else -> Unit
        }
    }

    private suspend fun loadTimelineIfNeeded() {
        when (timelineStateFlow.value) {
            is AsyncData.Uninitialized, is AsyncData.Failure -> {
                timelineStateFlow.emit(AsyncData.Loading())
                room.pinnedEventsTimeline()
                    .fold(
                        { timelineStateFlow.emit(AsyncData.Success(it)) },
                        { timelineStateFlow.emit(AsyncData.Failure(it)) }
                    )
            }
            else -> Unit
        }
    }
}
