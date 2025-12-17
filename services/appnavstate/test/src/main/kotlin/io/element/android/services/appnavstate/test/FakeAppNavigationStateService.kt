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
import io.element.android.libraries.matrix.api.core.SpaceId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.services.appnavstate.api.AppNavigationState
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.NavigationState
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppNavigationStateService(
    override val appNavigationState: MutableStateFlow<AppNavigationState> = MutableStateFlow(
        AppNavigationState(
            navigationState = NavigationState.Root,
            isInForeground = true,
        )
    ),
) : AppNavigationStateService {
    override fun onNavigateToSession(owner: String, sessionId: SessionId) = Unit
    override fun onLeavingSession(owner: String) = Unit

    override fun onNavigateToSpace(owner: String, spaceId: SpaceId) = Unit

    override fun onLeavingSpace(owner: String) = Unit

    override fun onNavigateToRoom(owner: String, roomId: RoomId) = Unit

    override fun onLeavingRoom(owner: String) = Unit

    override fun onNavigateToThread(owner: String, threadId: ThreadId) = Unit

    override fun onLeavingThread(owner: String) = Unit
}
