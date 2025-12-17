/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.appnavstate.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.annotations.AppCoroutineScope
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

private val loggerTag = LoggerTag("Navigation")

/**
 * TODO This will maybe not support properly navigation using permalink.
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultAppNavigationStateService(
    private val appForegroundStateService: AppForegroundStateService,
    @AppCoroutineScope
    coroutineScope: CoroutineScope,
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
            appForegroundStateService.startObservingForeground()
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
