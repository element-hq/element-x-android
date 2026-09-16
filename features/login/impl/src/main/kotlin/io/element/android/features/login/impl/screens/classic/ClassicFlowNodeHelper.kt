/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.classic

import dev.zacsweers.metro.Inject
import io.element.android.features.login.impl.classic.ElementClassicConnection
import io.element.android.features.login.impl.classic.ElementClassicConnectionState
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.api.toUserListFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

@Inject
class ClassicFlowNodeHelper(
    private val elementClassicConnection: ElementClassicConnection,
    private val sessionStore: SessionStore,
) {
    fun onResume() {
        elementClassicConnection.requestSession()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun navigationEventFlow(): Flow<NavigationEvent> {
        return elementClassicConnection.stateFlow
            .distinctUntilChangedBy {
                // Ignore change on ElementClassicConnectionState.ElementClassicReady.avatar
                if (it is ElementClassicConnectionState.ElementClassicReady) {
                    it.copy(avatar = null)
                } else {
                    it
                }
            }
            .flatMapLatest { elementClassicConnectionState ->
                when (elementClassicConnectionState) {
                    ElementClassicConnectionState.Idle -> {
                        // Ensure user is not stuck on the loading screen.
                        // If Element Classic is taking too long to communicate (or crashes), unblock the user after a few seconds.
                        flow {
                            emit(NavigationEvent.Idle)
                            delay(5_000)
                            emit(NavigationEvent.NavigateToOnBoarding)
                        }
                    }
                    ElementClassicConnectionState.ElementClassicNotFound,
                    ElementClassicConnectionState.ElementClassicReadyNoSession,
                    is ElementClassicConnectionState.Error -> {
                        flowOf(NavigationEvent.NavigateToOnBoarding)
                    }
                    is ElementClassicConnectionState.ElementClassicReady -> {
                        val existingSessions = sessionStore.sessionsFlow().toUserListFlow().first()
                        if (elementClassicConnectionState.elementClassicSession.userId.value in existingSessions) {
                            flowOf(NavigationEvent.NavigateToOnBoarding)
                        } else {
                            // 2 cases when this can be run:
                            // First time this screen will be displayed
                            // Missing key backup screen was displayed, but the data has changed (user set up the key backup on Classic),
                            // and the app is resuming.
                            flowOf(NavigationEvent.NavigateToLoginWithClassic(elementClassicConnectionState.elementClassicSession.userId))
                        }
                    }
                }
            }
    }
}
