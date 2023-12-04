/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
