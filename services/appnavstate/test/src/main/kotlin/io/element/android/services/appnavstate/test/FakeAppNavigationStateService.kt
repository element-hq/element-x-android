/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.appnavstate.test

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.services.appnavstate.api.AppNavigationState
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.NavigationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAppNavigationStateService(
    initialAppNavigationState: AppNavigationState = AppNavigationState(
        navigationState = NavigationState.Root,
        isInForeground = true,
    ),
) : AppNavigationStateService {
    private val _appNavigationState: MutableStateFlow<AppNavigationState> = MutableStateFlow(initialAppNavigationState)
    override val appNavigationState = _appNavigationState.asStateFlow()

    fun emitNavigationState(state: AppNavigationState) {
        _appNavigationState.value = state
    }

    override fun onNavigateToSession(owner: String, sessionId: SessionId) = Unit
    override fun onLeavingSession(owner: String) = Unit

    override fun onNavigateToRoom(owner: String, roomId: RoomId) = Unit
    override fun onLeavingRoom(owner: String) = Unit

    override fun onNavigateToThread(owner: String, threadId: ThreadId) = Unit
    override fun onLeavingThread(owner: String) = Unit
}
