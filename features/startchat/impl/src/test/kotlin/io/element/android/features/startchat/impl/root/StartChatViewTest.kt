/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.startchat.impl.root

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.core.content.getSystemService
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.common.truth.Truth.assertThat
import io.element.android.features.startchat.impl.R
import io.element.android.features.startchat.impl.userlist.aRecentDirectRoomList
import io.element.android.features.startchat.impl.userlist.aUserListState
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.model.getBestName
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.usersearch.api.UserSearchResult
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

class StartChatViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<StartChatEvent>(expectEvents = false)
        ensureCalledOnce {
            setStartChatView(
                aCreateRoomRootState(
                    eventSink = eventsRecorder,
                ),
                onCloseClick = it
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on New room invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<StartChatEvent>(expectEvents = false)
        ensureCalledOnce {
            setStartChatView(
                aCreateRoomRootState(
                    eventSink = eventsRecorder,
                ),
                onNewRoomClick = it
            )
            clickOn(R.string.screen_create_room_action_create_room)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on Invite people invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<StartChatEvent>(expectEvents = false)
        ensureCalledOnce {
            setStartChatView(
                aCreateRoomRootState(
                    applicationName = "test",
                    eventSink = eventsRecorder,
                ),
                onInviteFriendsClick = it
            )
            val text = activity!!.getString(CommonStrings.action_invite_friends_to_app, "test")
            onNodeWithText(text).performClick()
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on a user suggestion invokes the expected callback`() = runAndroidComposeUiTest {
        val recentDirectRoomList = aRecentDirectRoomList()
        val firstRoom = recentDirectRoomList[0]
        val eventsRecorder = EventsRecorder<StartChatEvent>(expectEvents = false)
        ensureCalledOnceWithParam(firstRoom.roomId) {
            setStartChatView(
                aCreateRoomRootState(
                    userListState = aUserListState(
                        recentDirectRooms = recentDirectRoomList
                    ),
                    eventSink = eventsRecorder,
                ),
                onOpenDM = it
            )
            onNodeWithText(firstRoom.matrixUser.getBestName()).performClick()
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on Join room by address invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<StartChatEvent>(expectEvents = false)
        ensureCalledOnce {
            setStartChatView(
                aCreateRoomRootState(
                    eventSink = eventsRecorder,
                ),
                onJoinRoomByAddressClick = it
            )
            clickOn(R.string.screen_start_chat_join_room_by_address_action)
        }
    }

    @Test
    fun `clicking on room directory invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<StartChatEvent>(expectEvents = false)
        ensureCalledOnce {
            setStartChatView(
                aCreateRoomRootState(
                    eventSink = eventsRecorder,
                ),
                onRoomDirectorySearchClick = it
            )
            clickOn(R.string.screen_room_directory_search_title)
        }
    }

    @Test
    fun `selecting a user while the keyboard is hidden starts the DM right away`() = runAndroidComposeUiTest {
        val matrixUser = aMatrixUser()
        val eventsRecorder = EventsRecorder<StartChatEvent>()
        setStartChatView(aStartChatStateWithSearchResult(matrixUser, eventsRecorder))
        mainClock.autoAdvance = false
        onNodeWithText(matrixUser.getBestName()).performClick()
        mainClock.advanceTimeByFrame()
        eventsRecorder.assertSingle(StartChatEvent.StartDM(matrixUser))
    }

    @Test
    fun `selecting a user while the keyboard is visible hides it and starts the DM once it is hidden`() = runAndroidComposeUiTest {
        val matrixUser = aMatrixUser()
        val eventsRecorder = EventsRecorder<StartChatEvent>()
        val view = setStartChatView(aStartChatStateWithSearchResult(matrixUser, eventsRecorder))
        val inputMethodManager = checkNotNull(view.context.getSystemService<InputMethodManager>())
        inputMethodManager.showSoftInput(view, 0)
        assertThat(shadowOf(inputMethodManager).isSoftInputVisible).isTrue()
        dispatchKeyboardInsets(view, isKeyboardVisible = true)
        mainClock.autoAdvance = false
        onNodeWithText(matrixUser.getBestName()).performClick()
        mainClock.advanceTimeByFrame()
        assertThat(shadowOf(inputMethodManager).isSoftInputVisible).isFalse()
        eventsRecorder.assertEmpty()
        mainClock.autoAdvance = true
        dispatchKeyboardInsets(view, isKeyboardVisible = false)
        eventsRecorder.assertSingle(StartChatEvent.StartDM(matrixUser))
    }

    @Test
    fun `selecting a user while the keyboard stays visible starts the DM after a timeout`() = runAndroidComposeUiTest {
        val matrixUser = aMatrixUser()
        val eventsRecorder = EventsRecorder<StartChatEvent>()
        val view = setStartChatView(aStartChatStateWithSearchResult(matrixUser, eventsRecorder))
        dispatchKeyboardInsets(view, isKeyboardVisible = true)
        mainClock.autoAdvance = false
        onNodeWithText(matrixUser.getBestName()).performClick()
        mainClock.advanceTimeBy(KeyboardHideTimeout.inWholeMilliseconds / 2)
        eventsRecorder.assertEmpty()
        mainClock.advanceTimeBy(KeyboardHideTimeout.inWholeMilliseconds)
        eventsRecorder.assertSingle(StartChatEvent.StartDM(matrixUser))
    }
}

private fun aStartChatStateWithSearchResult(
    matrixUser: MatrixUser,
    eventSink: (StartChatEvent) -> Unit,
) = aCreateRoomRootState(
    userListState = aUserListState(
        searchQuery = "query",
        isSearchActive = true,
        searchResults = SearchBarResultState.Results(persistentListOf(UserSearchResult(matrixUser))),
    ),
    eventSink = eventSink,
)

/**
 * Surfaces keyboard visibility to Compose via the window insets, matching system behavior.
 */
private fun AndroidComposeUiTest<ComponentActivity>.dispatchKeyboardInsets(view: View, isKeyboardVisible: Boolean) {
    runOnIdle {
        ViewCompat.dispatchApplyWindowInsets(
            view,
            WindowInsetsCompat.Builder()
                .setVisible(WindowInsetsCompat.Type.ime(), isKeyboardVisible)
                .setInsets(WindowInsetsCompat.Type.ime(), if (isKeyboardVisible) Insets.of(0, 0, 0, 300) else Insets.NONE)
                .build()
        )
    }
    waitForIdle()
}

private fun AndroidComposeUiTest<ComponentActivity>.setStartChatView(
    state: StartChatState,
    onCloseClick: () -> Unit = EnsureNeverCalled(),
    onNewRoomClick: () -> Unit = EnsureNeverCalled(),
    onOpenDM: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onInviteFriendsClick: () -> Unit = EnsureNeverCalled(),
    onJoinRoomByAddressClick: () -> Unit = EnsureNeverCalled(),
    onRoomDirectorySearchClick: () -> Unit = EnsureNeverCalled(),
): View {
    lateinit var view: View
    setContent {
        view = LocalView.current
        StartChatView(
            state = state,
            onCloseClick = onCloseClick,
            onNewRoomClick = onNewRoomClick,
            onOpenDM = onOpenDM,
            onInviteFriendsClick = onInviteFriendsClick,
            onJoinByAddressClick = onJoinRoomByAddressClick,
            onRoomDirectorySearchClick = onRoomDirectorySearchClick,
        )
    }
    return runOnIdle { view }
}
