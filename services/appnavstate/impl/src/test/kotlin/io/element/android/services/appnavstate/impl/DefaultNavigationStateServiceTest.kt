/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.appnavstate.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.A_SPACE_ID
import io.element.android.libraries.matrix.test.A_SPACE_ID_2
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID_2
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.NavigationState
import io.element.android.services.appnavstate.test.A_ROOM_OWNER
import io.element.android.services.appnavstate.test.A_SESSION_OWNER
import io.element.android.services.appnavstate.test.A_SPACE_OWNER
import io.element.android.services.appnavstate.test.A_THREAD_OWNER
import io.element.android.services.appnavstate.test.FakeAppForegroundStateService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultNavigationStateServiceTest {
    private val navigationStateRoot = NavigationState.Root
    private val navigationStateSession = NavigationState.Session(
        owner = A_SESSION_OWNER,
        sessionId = A_SESSION_ID
    )
    private val navigationStateSpace = NavigationState.Space(
        owner = A_SPACE_OWNER,
        spaceId = A_SPACE_ID,
        parentSession = navigationStateSession
    )
    private val navigationStateRoom = NavigationState.Room(
        owner = A_ROOM_OWNER,
        roomId = A_ROOM_ID,
        parentSpace = navigationStateSpace
    )
    private val navigationStateThread = NavigationState.Thread(
        owner = A_THREAD_OWNER,
        threadId = A_THREAD_ID,
        parentRoom = navigationStateRoom
    )

    @Test
    fun testNavigation() = runTest {
        val service = createStateService()
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoot)
        service.onNavigateToSession(A_SESSION_OWNER, A_SESSION_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSession)
        service.onNavigateToSpace(A_SPACE_OWNER, A_SPACE_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSpace)
        service.onNavigateToRoom(A_ROOM_OWNER, A_ROOM_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoom)
        service.onNavigateToThread(A_THREAD_OWNER, A_THREAD_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateThread)
        // Leaving the states
        service.onLeavingThread(A_THREAD_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoom)
        service.onLeavingRoom(A_ROOM_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSpace)
        service.onLeavingSpace(A_SPACE_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSession)
        service.onLeavingSession(A_SESSION_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoot)
    }

    @Test
    fun testFailure() = runTest {
        val service = createStateService()
        service.onNavigateToSpace(A_SPACE_OWNER, A_SPACE_ID)
        assertThat(service.appNavigationState.value.navigationState).isEqualTo(NavigationState.Root)
    }

    @Test
    fun testOnNavigateToThread() = runTest {
        val service = createStateService()
        // From root (no effect)
        service.onNavigateToThread(A_THREAD_OWNER, A_THREAD_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoot)
        // From session (no effect)
        service.reset()
        service.navigateToSession()
        service.onNavigateToThread(A_THREAD_OWNER, A_THREAD_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSession)
        // From space (no effect)
        service.reset()
        service.navigateToSpace()
        service.onNavigateToThread(A_THREAD_OWNER, A_THREAD_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSpace)
        // From room
        service.reset()
        service.navigateToRoom()
        service.onNavigateToThread(A_THREAD_OWNER, A_THREAD_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateThread)
        // From thread
        service.reset()
        service.navigateToThread()
        // Navigate to another thread
        service.onNavigateToThread(A_THREAD_OWNER, A_THREAD_ID_2)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateThread.copy(threadId = A_THREAD_ID_2))
    }

    @Test
    fun testOnNavigateToRoom() = runTest {
        val service = createStateService()
        // From root (no effect)
        service.onNavigateToRoom(A_ROOM_OWNER, A_ROOM_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoot)
        // From session (no effect)
        service.reset()
        service.navigateToSession()
        service.onNavigateToRoom(A_ROOM_OWNER, A_ROOM_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSession)
        // From space
        service.reset()
        service.navigateToSpace()
        service.onNavigateToRoom(A_ROOM_OWNER, A_ROOM_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoom)
        // From room
        service.reset()
        service.navigateToRoom()
        // Navigate to another room
        service.onNavigateToRoom(A_ROOM_OWNER, A_ROOM_ID_2)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoom.copy(roomId = A_ROOM_ID_2))
        // From thread
        service.reset()
        service.navigateToThread()
        service.onNavigateToRoom(A_ROOM_OWNER, A_ROOM_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoom)
    }

    @Test
    fun testOnNavigateToSpace() = runTest {
        val service = createStateService()
        // From root (no effect)
        service.onNavigateToSpace(A_SPACE_OWNER, A_SPACE_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoot)
        // From session
        service.reset()
        service.navigateToSession()
        service.onNavigateToSpace(A_SPACE_OWNER, A_SPACE_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSpace)
        // From space
        service.reset()
        service.navigateToSpace()
        // Navigate to another space
        service.onNavigateToSpace(A_SPACE_OWNER, A_SPACE_ID_2)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSpace.copy(spaceId = A_SPACE_ID_2))
        // From room (no effect)
        service.reset()
        service.navigateToRoom()
        service.onNavigateToSpace(A_SPACE_OWNER, A_SPACE_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSpace)
        // From thread (no effect)
        service.reset()
        service.navigateToThread()
        service.onNavigateToSpace(A_SPACE_OWNER, A_SPACE_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSpace)
    }

    @Test
    fun testOnNavigateToSession() = runTest {
        val service = createStateService()
        // From root
        service.onNavigateToSession(A_SESSION_OWNER, A_SESSION_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSession)
        // From session
        service.reset()
        service.navigateToSession()
        // Navigate to another session
        service.onNavigateToSession(A_SESSION_OWNER, A_SESSION_ID_2)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSession.copy(sessionId = A_SESSION_ID_2))
        // From space
        service.reset()
        service.navigateToSpace()
        service.onNavigateToSession(A_SESSION_OWNER, A_SESSION_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSession)
        // From room
        service.reset()
        service.navigateToRoom()
        service.onNavigateToSession(A_SESSION_OWNER, A_SESSION_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSession)
        // From thread
        service.reset()
        service.navigateToThread()
        service.onNavigateToSession(A_SESSION_OWNER, A_SESSION_ID)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSession)
    }

    @Test
    fun testOnLeavingThread() = runTest {
        val service = createStateService()
        // From root (no effect)
        service.onLeavingThread(A_THREAD_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoot)
        // From session (no effect)
        service.reset()
        service.navigateToSession()
        service.onLeavingThread(A_THREAD_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSession)
        // From space (no effect)
        service.reset()
        service.navigateToSpace()
        service.onLeavingThread(A_THREAD_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSpace)
        // From room (no effect)
        service.reset()
        service.navigateToRoom()
        service.onLeavingThread(A_THREAD_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoom)
        // From thread
        service.reset()
        service.navigateToThread()
        service.onLeavingThread(A_THREAD_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoom)
    }

    @Test
    fun testOnLeavingRoom() = runTest {
        val service = createStateService()
        // From root (no effect)
        service.onLeavingRoom(A_ROOM_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoot)
        // From session (no effect)
        service.reset()
        service.navigateToSession()
        service.onLeavingRoom(A_ROOM_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSession)
        // From space (no effect)
        service.reset()
        service.navigateToSpace()
        service.onLeavingRoom(A_ROOM_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSpace)
        // From room
        service.reset()
        service.navigateToRoom()
        service.onLeavingRoom(A_ROOM_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSpace)
        // From thread (no effect)
        service.reset()
        service.navigateToThread()
        service.onLeavingRoom(A_ROOM_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateThread)
    }

    @Test
    fun testOnLeavingSpace() = runTest {
        val service = createStateService()
        // From root (no effect)
        service.onLeavingSpace(A_SPACE_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoot)
        // From session (no effect)
        service.reset()
        service.navigateToSession()
        service.onLeavingSpace(A_SPACE_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSession)
        // From space
        service.reset()
        service.navigateToSpace()
        service.onLeavingSpace(A_SPACE_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSession)
        // From room (no effect)
        service.reset()
        service.navigateToRoom()
        service.onLeavingSpace(A_SPACE_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoom)
        // From thread (no effect)
        service.reset()
        service.navigateToThread()
        service.onLeavingSpace(A_SPACE_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateThread)
    }

    @Test
    fun testOnLeavingSession() = runTest {
        val service = createStateService()
        // From root
        service.onLeavingSession(A_SESSION_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoot)
        // From session
        service.reset()
        service.navigateToSession()
        service.onLeavingSession(A_SESSION_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoot)
        // From space (no effect)
        service.reset()
        service.navigateToSpace()
        service.onLeavingSession(A_SESSION_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateSpace)
        // From room (no effect)
        service.reset()
        service.navigateToRoom()
        service.onLeavingSession(A_SESSION_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateRoom)
        // From thread (no effect)
        service.reset()
        service.navigateToThread()
        service.onLeavingSession(A_SESSION_OWNER)
        assertThat(service.appNavigationState.first().navigationState).isEqualTo(navigationStateThread)
    }

    private fun AppNavigationStateService.reset() {
        navigateToSession()
        onLeavingSession(A_SESSION_OWNER)
    }

    private fun AppNavigationStateService.navigateToSession() {
        onNavigateToSession(A_SESSION_OWNER, A_SESSION_ID)
    }

    private fun AppNavigationStateService.navigateToSpace() {
        navigateToSession()
        onNavigateToSpace(A_SPACE_OWNER, A_SPACE_ID)
    }

    private fun AppNavigationStateService.navigateToRoom() {
        navigateToSpace()
        onNavigateToRoom(A_ROOM_OWNER, A_ROOM_ID)
    }

    private fun AppNavigationStateService.navigateToThread() {
        navigateToRoom()
        onNavigateToThread(A_THREAD_OWNER, A_THREAD_ID)
    }

    private fun TestScope.createStateService() = DefaultAppNavigationStateService(
        appForegroundStateService = FakeAppForegroundStateService(),
        coroutineScope = backgroundScope,
    )
}
