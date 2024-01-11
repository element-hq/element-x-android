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

package io.element.android.services.appnavstate.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.SpaceId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.services.appnavstate.api.AppForegroundStateService
import io.element.android.services.appnavstate.api.AppNavigationState
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.NavigationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("Navigation")

/**
 * TODO This will maybe not support properly navigation using permalink.
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultAppNavigationStateService @Inject constructor(
    private val appForegroundStateService: AppForegroundStateService,
    private val coroutineScope: CoroutineScope,
) : AppNavigationStateService {
    private val state = MutableStateFlow(
        AppNavigationState(
            navigationState = NavigationState.Root,
            isInForeground = true,
        )
    )
    override val appNavigationState: StateFlow<AppNavigationState> = state

    init {
        coroutineScope.launch {
            appForegroundStateService.start()
            appForegroundStateService.isInForeground.collect { isInForeground ->
                state.getAndUpdate { it.copy(isInForeground = isInForeground) }
            }
        }
    }

    override fun onNavigateToSession(owner: String, sessionId: SessionId) {
        val currentValue = state.value.navigationState
        Timber.tag(loggerTag.value).d("Navigating to session $sessionId. Current state: $currentValue")
        val newValue: NavigationState.Session = when (currentValue) {
            is NavigationState.Session,
            is NavigationState.Space,
            is NavigationState.Room,
            is NavigationState.Thread,
            is NavigationState.Root -> NavigationState.Session(owner, sessionId)
        }
        state.getAndUpdate { it.copy(navigationState = newValue) }
    }

    override fun onNavigateToSpace(owner: String, spaceId: SpaceId) {
        val currentValue = state.value.navigationState
        Timber.tag(loggerTag.value).d("Navigating to space $spaceId. Current state: $currentValue")
        val newValue: NavigationState.Space = when (currentValue) {
            NavigationState.Root -> return logError("onNavigateToSession()")
            is NavigationState.Session -> NavigationState.Space(owner, spaceId, currentValue)
            is NavigationState.Space -> NavigationState.Space(owner, spaceId, currentValue.parentSession)
            is NavigationState.Room -> NavigationState.Space(owner, spaceId, currentValue.parentSpace.parentSession)
            is NavigationState.Thread -> NavigationState.Space(owner, spaceId, currentValue.parentRoom.parentSpace.parentSession)
        }
        state.getAndUpdate { it.copy(navigationState = newValue) }
    }

    override fun onNavigateToRoom(owner: String, roomId: RoomId) {
        val currentValue = state.value.navigationState
        Timber.tag(loggerTag.value).d("Navigating to room $roomId. Current state: $currentValue")
        val newValue: NavigationState.Room = when (currentValue) {
            NavigationState.Root -> return logError("onNavigateToSession()")
            is NavigationState.Session -> return logError("onNavigateToSpace()")
            is NavigationState.Space -> NavigationState.Room(owner, roomId, currentValue)
            is NavigationState.Room -> NavigationState.Room(owner, roomId, currentValue.parentSpace)
            is NavigationState.Thread -> NavigationState.Room(owner, roomId, currentValue.parentRoom.parentSpace)
        }
        state.getAndUpdate { it.copy(navigationState = newValue) }
    }

    override fun onNavigateToThread(owner: String, threadId: ThreadId) {
        val currentValue = state.value.navigationState
        Timber.tag(loggerTag.value).d("Navigating to thread $threadId. Current state: $currentValue")
        val newValue: NavigationState.Thread = when (currentValue) {
            NavigationState.Root -> return logError("onNavigateToSession()")
            is NavigationState.Session -> return logError("onNavigateToSpace()")
            is NavigationState.Space -> return logError("onNavigateToRoom()")
            is NavigationState.Room -> NavigationState.Thread(owner, threadId, currentValue)
            is NavigationState.Thread -> NavigationState.Thread(owner, threadId, currentValue.parentRoom)
        }
        state.getAndUpdate { it.copy(navigationState = newValue) }
    }

    override fun onLeavingThread(owner: String) {
        val currentValue = state.value.navigationState
        Timber.tag(loggerTag.value).d("Leaving thread. Current state: $currentValue")
        if (!currentValue.assertOwner(owner)) return
        val newValue: NavigationState.Room = when (currentValue) {
            NavigationState.Root -> return logError("onNavigateToSession()")
            is NavigationState.Session -> return logError("onNavigateToSpace()")
            is NavigationState.Space -> return logError("onNavigateToRoom()")
            is NavigationState.Room -> return logError("onNavigateToThread()")
            is NavigationState.Thread -> currentValue.parentRoom
        }
        state.getAndUpdate { it.copy(navigationState = newValue) }
    }

    override fun onLeavingRoom(owner: String) {
        val currentValue = state.value.navigationState
        Timber.tag(loggerTag.value).d("Leaving room. Current state: $currentValue")
        if (!currentValue.assertOwner(owner)) return
        val newValue: NavigationState.Space = when (currentValue) {
            NavigationState.Root -> return logError("onNavigateToSession()")
            is NavigationState.Session -> return logError("onNavigateToSpace()")
            is NavigationState.Space -> return logError("onNavigateToRoom()")
            is NavigationState.Room -> currentValue.parentSpace
            is NavigationState.Thread -> currentValue.parentRoom.parentSpace
        }
        state.getAndUpdate { it.copy(navigationState = newValue) }
    }

    override fun onLeavingSpace(owner: String) {
        val currentValue = state.value.navigationState
        Timber.tag(loggerTag.value).d("Leaving space. Current state: $currentValue")
        if (!currentValue.assertOwner(owner)) return
        val newValue: NavigationState.Session = when (currentValue) {
            NavigationState.Root -> return logError("onNavigateToSession()")
            is NavigationState.Session -> return logError("onNavigateToSpace()")
            is NavigationState.Space -> currentValue.parentSession
            is NavigationState.Room -> currentValue.parentSpace.parentSession
            is NavigationState.Thread -> currentValue.parentRoom.parentSpace.parentSession
        }
        state.getAndUpdate { it.copy(navigationState = newValue) }
    }

    override fun onLeavingSession(owner: String) {
        val currentValue = state.value.navigationState
        Timber.tag(loggerTag.value).d("Leaving session. Current state: $currentValue")
        if (!currentValue.assertOwner(owner)) return
        state.getAndUpdate { it.copy(navigationState = NavigationState.Root) }
    }

    private fun logError(logPrefix: String) {
        Timber.tag(loggerTag.value).w("$logPrefix must be call first.")
    }

    private fun NavigationState.assertOwner(owner: String): Boolean {
        if (this.owner != owner) {
            Timber.tag(loggerTag.value).d("Can't leave current state as the owner is not the same (current = ${this.owner}, new = $owner)")
            return false
        }
        return true
    }
}
