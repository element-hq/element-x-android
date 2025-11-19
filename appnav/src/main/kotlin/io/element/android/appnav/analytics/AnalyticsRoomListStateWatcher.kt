/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.analytics

import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.withPreviousValue
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction.ResumeAppUntilNewRoomsReceived
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.appnavstate.api.AppNavigationStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This component is used to check how long it takes for the room list to be up to date after opening the app while it's on a 'warm' state:
 * the app was previously running and we just returned to it.
 */
@Inject
class AnalyticsRoomListStateWatcher(
    private val appNavigationStateService: AppNavigationStateService,
    private val roomListService: RoomListService,
    private val analyticsService: AnalyticsService,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
) {
    private var currentCoroutineScope: CoroutineScope? = null
    private val isWarmState = AtomicBoolean(false)

    fun start() {
        if (currentCoroutineScope != null) {
            Timber.w("Can't start RoomListStateWatcher, it's already running.")
            return
        }

        val coroutineScope = CoroutineScope(sessionCoroutineScope.coroutineContext + dispatchers.computation)
        appNavigationStateService.appNavigationState
            .map { it.isInForeground }
            .distinctUntilChanged()
            .withPreviousValue()
            .onEach { (wasInForeground, isInForeground) ->
                if (isInForeground && roomListService.state.value != RoomListService.State.Running) {
                    analyticsService.startLongRunningTransaction(ResumeAppUntilNewRoomsReceived)
                }

                if (wasInForeground == false && isInForeground) {
                    isWarmState.set(true)
                }
            }
            .launchIn(coroutineScope)

        roomListService.state
            .onEach { state ->
                if (state == RoomListService.State.Running && isWarmState.get()) {
                    analyticsService.removeLongRunningTransaction(ResumeAppUntilNewRoomsReceived)?.finish()
                }
            }
            .launchIn(coroutineScope)

        currentCoroutineScope = coroutineScope
    }

    fun stop() {
        currentCoroutineScope?.cancel()
        currentCoroutineScope = null
    }
}
