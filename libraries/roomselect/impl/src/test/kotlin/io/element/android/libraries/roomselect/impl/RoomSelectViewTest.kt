/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.libraries.roomselect.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.components.aSelectRoomInfo
import io.element.android.libraries.roomselect.api.RoomSelectMode
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test
import org.robolectric.annotation.Config

class RoomSelectViewTest : RobolectricTest() {
    @Config(qualifiers = "h1024dp")
    @Test
    fun `a room with a canonical alias renders the alias`() = runAndroidComposeUiTest {
        setRoomSelectView(
            aRoomSelectState(
                resultState = SearchBarResultState.Results(aRoomSelectRoomList()),
            ),
        )
        onNodeWithText("Room with alias").assertIsDisplayed()
        onNodeWithText("#alias:example.org").assertIsDisplayed()
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `a direct message without alias renders the matrix id of the other user`() = runAndroidComposeUiTest {
        setRoomSelectView(
            aRoomSelectState(
                resultState = SearchBarResultState.Results(aRoomSelectRoomList()),
            ),
        )
        onNodeWithText("Alice").assertIsDisplayed()
        onNodeWithText("@alice:example.org").assertIsDisplayed()
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `a room which is not a direct message does not render the matrix id of its hero`() = runAndroidComposeUiTest {
        setRoomSelectView(
            aRoomSelectState(
                resultState = SearchBarResultState.Results(
                    persistentListOf(
                        aSelectRoomInfo(
                            roomId = RoomId("!room:domain"),
                            name = "Room with a single hero",
                            heroes = persistentListOf(
                                aMatrixUser(id = "@alice:example.org", displayName = "Alice"),
                            ),
                        ),
                    )
                ),
            ),
        )
        onNodeWithText("Room with a single hero").assertIsDisplayed()
        onNodeWithText("@alice:example.org").assertDoesNotExist()
    }

    @Test
    fun `clicking on switch account emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomSelectEvent>()
        setRoomSelectView(
            aRoomSelectState(
                mode = RoomSelectMode.Share,
                selectedAccount = aMatrixUser(id = "@alice:example.org", displayName = "Alice"),
                otherAccounts = persistentListOf(aMatrixUser(id = "@bob:example.org", displayName = "Bob")),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.common_switch_account)
        // The first event is the initial UpdateVisibleRange
        eventsRecorder.assertList(listOf(RoomSelectEvent.UpdateVisibleRange(0..-1), RoomSelectEvent.ToggleAccountListExpanded))
    }

    @Test
    fun `clicking on another account emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomSelectEvent>()
        setRoomSelectView(
            aRoomSelectState(
                mode = RoomSelectMode.Share,
                selectedAccount = aMatrixUser(id = "@alice:example.org", displayName = "Alice"),
                otherAccounts = persistentListOf(aMatrixUser(id = "@bob:example.org", displayName = "Bob")),
                isAccountListExpanded = true,
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText("Bob").performClick()
        // The first event is the initial UpdateVisibleRange
        eventsRecorder.assertList(listOf(RoomSelectEvent.UpdateVisibleRange(0..-1), RoomSelectEvent.SelectAccount(UserId("@bob:example.org"))))
    }

    @Test
    fun `the account switch is not displayed if there is no other account`() = runAndroidComposeUiTest {
        setRoomSelectView(
            aRoomSelectState(
                mode = RoomSelectMode.Share,
                selectedAccount = aMatrixUser(id = "@alice:example.org", displayName = "Alice"),
            ),
        )
        onNodeWithText(activity!!.getString(CommonStrings.common_switch_account)).assertDoesNotExist()
    }

    @Test
    fun `the room list can be scrolled when there is no other account`() = runAndroidComposeUiTest {
        setRoomSelectView(
            aRoomSelectState(
                mode = RoomSelectMode.Share,
                resultState = SearchBarResultState.Results(aLongRoomList()),
            ),
        )
        onNodeWithText("Room 0").assertIsDisplayed()
        onRoot().performTouchInput { swipeUp() }
        onNodeWithText("Room 0").assertIsNotDisplayed()
    }

    @Test
    fun `the room list can be scrolled when there are other accounts`() = runAndroidComposeUiTest {
        setRoomSelectView(
            aRoomSelectState(
                mode = RoomSelectMode.Share,
                resultState = SearchBarResultState.Results(aLongRoomList()),
                otherAccounts = persistentListOf(aMatrixUser(id = "@bob:example.org", displayName = "Bob")),
            ),
        )
        onNodeWithText("Room 0").assertIsDisplayed()
        onRoot().performTouchInput { swipeUp() }
        onNodeWithText("Room 0").assertIsNotDisplayed()
    }

    @Test
    fun `an error is displayed instead of the rooms if they cannot be loaded`() = runAndroidComposeUiTest<ComponentActivity> {
        setRoomSelectView(
            aRoomSelectState(
                resultState = SearchBarResultState.Results(aRoomSelectRoomList()),
                hasRoomListError = true,
            ),
        )
        onNodeWithText(activity!!.getString(CommonStrings.common_something_went_wrong)).assertIsDisplayed()
        onNodeWithText(activity!!.getString(R.string.screen_room_select_error_cannot_load_rooms)).assertIsDisplayed()
        onNodeWithText("Room with alias").assertDoesNotExist()
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setRoomSelectView(
    state: RoomSelectState,
    onDismiss: () -> Unit = EnsureNeverCalled(),
    onSubmit: (List<RoomId>) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        RoomSelectView(
            state = state,
            onDismiss = onDismiss,
            onSubmit = onSubmit,
        )
    }
}

private fun aLongRoomList() = List(30) { index ->
    aSelectRoomInfo(
        roomId = RoomId("!room$index:domain"),
        name = "Room $index",
    )
}.toImmutableList()
