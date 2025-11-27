/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.roomlist

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.home.impl.HomeView
import io.element.android.features.home.impl.R
import io.element.android.features.home.impl.aHomeState
import io.element.android.features.home.impl.components.RoomListMenuAction
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.features.home.impl.model.RoomSummaryDisplayType
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.setSafeContent
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class RoomListViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Config(qualifiers = "h1024dp")
    @Test
    fun `displaying the view automatically sends a couple of UpdateVisibleRangeEvents`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        rule.setRoomListView(
            state = aRoomListState(
                contentState = aRoomsContentState(securityBannerState = SecurityBannerState.RecoveryKeyConfirmation),
                eventSink = eventsRecorder,
            )
        )

        eventsRecorder.assertList(
            listOf(
                RoomListEvents.UpdateVisibleRange(IntRange.EMPTY),
                RoomListEvents.UpdateVisibleRange(0..5),
            )
        )
    }

    @Test
    fun `clicking on close recovery key banner emits the expected Event`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        rule.setRoomListView(
            state = aRoomListState(
                contentState = aRoomsContentState(securityBannerState = SecurityBannerState.RecoveryKeyConfirmation),
                eventSink = eventsRecorder,
            )
        )

        // Remove automatic initial events
        eventsRecorder.clear()

        val close = rule.activity.getString(CommonStrings.action_close)
        rule.onNodeWithContentDescription(close).performClick()
        eventsRecorder.assertSingle(RoomListEvents.DismissBanner)
    }

    @Test
    fun `clicking on close setup key banner emits the expected Event`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        rule.setRoomListView(
            state = aRoomListState(
                contentState = aRoomsContentState(securityBannerState = SecurityBannerState.SetUpRecovery),
                eventSink = eventsRecorder,
            )
        )

        // Remove automatic initial events
        eventsRecorder.clear()

        val close = rule.activity.getString(CommonStrings.action_close)
        rule.onNodeWithContentDescription(close).performClick()
        eventsRecorder.assertSingle(RoomListEvents.DismissBanner)
    }

    @Test
    fun `clicking on continue recovery key banner invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        ensureCalledOnce { callback ->
            rule.setRoomListView(
                state = aRoomListState(
                    contentState = aRoomsContentState(securityBannerState = SecurityBannerState.RecoveryKeyConfirmation),
                    eventSink = eventsRecorder,
                ),
                onConfirmRecoveryKeyClick = callback,
            )

            // Remove automatic initial events
            eventsRecorder.clear()

            rule.clickOn(CommonStrings.action_continue)

            eventsRecorder.assertEmpty()
        }
    }

    @Test
    fun `clicking on continue setup key banner invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        ensureCalledOnce { callback ->
            rule.setRoomListView(
                state = aRoomListState(
                    contentState = aRoomsContentState(securityBannerState = SecurityBannerState.SetUpRecovery),
                    eventSink = eventsRecorder,
                ),
                onSetUpRecoveryClick = callback,
            )
            // Remove automatic initial events
            eventsRecorder.clear()
            rule.clickOn(R.string.banner_set_up_recovery_submit)
            eventsRecorder.assertEmpty()
        }
    }

    @Test
    fun `clicking on start chat when the session has no room invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setRoomListView(
                state = aRoomListState(
                    eventSink = eventsRecorder,
                    contentState = anEmptyContentState(),
                ),
                onCreateRoomClick = callback,
            )
            rule.clickOn(CommonStrings.action_start_chat)
        }
    }

    @Test
    fun `clicking on a room invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        val state = aRoomListState(
            eventSink = eventsRecorder,
        )
        val room0 = state.contentAsRooms().summaries.first {
            it.displayType == RoomSummaryDisplayType.ROOM
        }
        ensureCalledOnceWithParam(room0.roomId) { callback ->
            rule.setRoomListView(
                state = state,
                onRoomClick = callback,
            )

            // Remove automatic initial events
            eventsRecorder.clear()

            rule.onNodeWithText(room0.latestEvent!!.toString()).performClick()
        }

        eventsRecorder.assertEmpty()
    }

    @Test
    fun `clicking on a room twice invokes the expected callback only once`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        val state = aRoomListState(
            eventSink = eventsRecorder,
        )
        val room0 = state.contentAsRooms().summaries.first {
            it.displayType == RoomSummaryDisplayType.ROOM
        }
        ensureCalledOnceWithParam(room0.roomId) { callback ->
            rule.setRoomListView(
                state = state,
                onRoomClick = callback,
            )
            // Remove automatic initial events
            eventsRecorder.clear()
            rule.onNodeWithText(room0.latestEvent!!.toString())
                .performClick()
                .performClick()
        }
        eventsRecorder.assertEmpty()
    }

    @Test
    fun `long clicking on a room emits the expected Event`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        val state = aRoomListState(
            eventSink = eventsRecorder,
        )
        val room0 = state.contentAsRooms().summaries.first {
            it.displayType == RoomSummaryDisplayType.ROOM
        }
        rule.setRoomListView(
            state = state,
        )
        // Remove automatic initial events
        eventsRecorder.clear()

        rule.onNodeWithText(room0.latestEvent!!.toString()).performTouchInput { longClick() }
        eventsRecorder.assertSingle(RoomListEvents.ShowContextMenu(room0))
    }

    @Test
    fun `clicking on a room setting invokes the expected callback and emits expected Event`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        val state = aRoomListState(
            contextMenu = aContextMenuShown(),
            eventSink = eventsRecorder,
        )
        val room0 = (state.contextMenu as RoomListState.ContextMenu.Shown).roomId
        ensureCalledOnceWithParam(room0) { callback ->
            rule.setRoomListView(
                state = state,
                onRoomSettingsClick = callback,
            )

            // Remove automatic initial events
            eventsRecorder.clear()

            rule.clickOn(CommonStrings.common_settings)
        }

        eventsRecorder.assertSingle(RoomListEvents.HideContextMenu)
    }

    @Test
    fun `clicking on accept and decline invite emits the expected Events`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        val state = aRoomListState(
            eventSink = eventsRecorder,
        )
        val invitedRoom = state.contentAsRooms().summaries.first {
            it.displayType == RoomSummaryDisplayType.INVITE
        }
        rule.setRoomListView(state = state)

        // Remove automatic initial events
        eventsRecorder.clear()

        rule.clickOn(CommonStrings.action_accept)
        rule.clickOn(CommonStrings.action_decline)
        eventsRecorder.assertList(
            listOf(
                RoomListEvents.AcceptInvite(invitedRoom),
                RoomListEvents.ShowDeclineInviteMenu(invitedRoom),
            )
        )
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRoomListView(
    state: RoomListState,
    onRoomClick: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onSettingsClick: () -> Unit = EnsureNeverCalled(),
    onSetUpRecoveryClick: () -> Unit = EnsureNeverCalled(),
    onConfirmRecoveryKeyClick: () -> Unit = EnsureNeverCalled(),
    onCreateRoomClick: () -> Unit = EnsureNeverCalled(),
    onRoomSettingsClick: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onMenuActionClick: (RoomListMenuAction) -> Unit = EnsureNeverCalledWithParam(),
    onReportRoomClick: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onDeclineInviteAndBlockUser: (RoomListRoomSummary) -> Unit = EnsureNeverCalledWithParam(),
) {
    setSafeContent {
        HomeView(
            homeState = aHomeState(roomListState = state),
            onRoomClick = onRoomClick,
            onSettingsClick = onSettingsClick,
            onSetUpRecoveryClick = onSetUpRecoveryClick,
            onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
            onStartChatClick = onCreateRoomClick,
            onRoomSettingsClick = onRoomSettingsClick,
            onMenuActionClick = onMenuActionClick,
            onDeclineInviteAndBlockUser = onDeclineInviteAndBlockUser,
            onReportRoomClick = onReportRoomClick,
            acceptDeclineInviteView = {},
            leaveRoomView = {},
        )
    }
}
