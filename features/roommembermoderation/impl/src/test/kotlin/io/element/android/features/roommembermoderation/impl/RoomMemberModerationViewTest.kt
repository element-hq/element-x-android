/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.roommembermoderation.api.ModerationAction
import io.element.android.features.roommembermoderation.api.ModerationActionState
import io.element.android.features.roommembermoderation.api.RoomMemberModerationEvents
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.testtags.TestTags
import io.element.android.tests.testutils.EnsureNeverCalledWithTwoParams
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnceWithTwoParams
import io.element.android.tests.testutils.pressTag
import io.element.android.tests.testutils.setSafeContent
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomMemberModerationViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on display profile action calls onSelectAction`() {
        val user = anAlice()
        val eventsRecorder = EventsRecorder<RoomMemberModerationEvents>(expectEvents = false)
        ensureCalledOnceWithTwoParams<ModerationAction, MatrixUser>(ModerationAction.DisplayProfile, user) { callback ->
            rule.setRoomMemberModerationView(
                aRoomMembersModerationState(
                    selectedUser = user,
                    actions = listOf(
                        ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                    ),
                    eventSink = eventsRecorder
                ),
                onSelectAction = callback
            )
            rule.clickOn(R.string.screen_bottom_sheet_manage_room_member_member_user_info)
        }
    }

    @Test
    fun `clicking on kick user action calls onSelectAction`() {
        val user = anAlice()
        val eventsRecorder = EventsRecorder<RoomMemberModerationEvents>(expectEvents = false)
        ensureCalledOnceWithTwoParams<ModerationAction, MatrixUser>(ModerationAction.KickUser, user) { callback ->
            rule.setRoomMemberModerationView(
                aRoomMembersModerationState(
                    selectedUser = user,
                    actions = listOf(
                        ModerationActionState(action = ModerationAction.KickUser, isEnabled = true),
                    ),
                    eventSink = eventsRecorder
                ),
                onSelectAction = callback
            )
            rule.clickOn(R.string.screen_bottom_sheet_manage_room_member_remove)
            // Gives time for bottomsheet to hide
            rule.mainClock.advanceTimeBy(1_000)
        }
    }

    @Test
    fun `clicking on ban user action calls onSelectAction`() {
        val user = anAlice()
        val eventsRecorder = EventsRecorder<RoomMemberModerationEvents>(expectEvents = false)
        ensureCalledOnceWithTwoParams<ModerationAction, MatrixUser>(ModerationAction.BanUser, user) { callback ->
            rule.setRoomMemberModerationView(
                aRoomMembersModerationState(
                    selectedUser = user,
                    actions = listOf(
                        ModerationActionState(action = ModerationAction.BanUser, isEnabled = true),
                    ),
                    eventSink = eventsRecorder
                ),
                onSelectAction = callback
            )
            rule.clickOn(R.string.screen_bottom_sheet_manage_room_member_ban)
            // Gives time for bottomsheet to hide
            rule.mainClock.advanceTimeBy(1_000)
        }
    }

    @Test
    fun `clicking on unban user action calls onSelectAction`() {
        val user = anAlice()
        val eventsRecorder = EventsRecorder<RoomMemberModerationEvents>(expectEvents = false)
        ensureCalledOnceWithTwoParams<ModerationAction, MatrixUser>(ModerationAction.UnbanUser, user) { callback ->
            rule.setRoomMemberModerationView(
                aRoomMembersModerationState(
                    selectedUser = user,
                    actions = listOf(
                        ModerationActionState(action = ModerationAction.UnbanUser, isEnabled = true),
                    ),
                    eventSink = eventsRecorder
                ),
                onSelectAction = callback
            )
            rule.clickOn(R.string.screen_bottom_sheet_manage_room_member_unban)
            // Gives time for bottomsheet to hide
            rule.mainClock.advanceTimeBy(1_000)
        }
    }

    @Test
    fun `clicking submit on kick confirmation dialog sends DoKickUser event`() {
        val eventsRecorder = EventsRecorder<RoomMemberModerationEvents>()
        rule.setRoomMemberModerationView(
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                kickUserAsyncAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder
            ),
        )
        rule.pressTag(TestTags.dialogPositive.value)
        eventsRecorder.assertSingle(InternalRoomMemberModerationEvents.DoKickUser(reason = ""))
    }

    @Test
    fun `clicking dismiss on kick confirmation dialog sends Reset event`() {
        val eventsRecorder = EventsRecorder<RoomMemberModerationEvents>()
        rule.setRoomMemberModerationView(
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                kickUserAsyncAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder
            ),
        )
        rule.pressTag(TestTags.dialogNegative.value)
        eventsRecorder.assertSingle(InternalRoomMemberModerationEvents.Reset)
    }

    @Test
    fun `clicking submit on ban confirmation dialog sends DoBanUser event`() {
        val eventsRecorder = EventsRecorder<RoomMemberModerationEvents>()
        rule.setRoomMemberModerationView(
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                banUserAsyncAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder
            ),
        )
        rule.pressTag(TestTags.dialogPositive.value)
        eventsRecorder.assertSingle(InternalRoomMemberModerationEvents.DoBanUser(reason = ""))
    }

    @Test
    fun `clicking dismiss on ban confirmation dialog sends Reset event`() {
        val eventsRecorder = EventsRecorder<RoomMemberModerationEvents>()
        rule.setRoomMemberModerationView(
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                banUserAsyncAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder
            ),
        )
        rule.pressTag(TestTags.dialogNegative.value)
        eventsRecorder.assertSingle(InternalRoomMemberModerationEvents.Reset)
    }

    @Test
    fun `clicking confirm on unban confirmation dialog sends DoUnbanUser event`() {
        val eventsRecorder = EventsRecorder<RoomMemberModerationEvents>()
        rule.setRoomMemberModerationView(
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                unbanUserAsyncAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder
            ),
        )
        rule.pressTag(TestTags.dialogPositive.value)
        eventsRecorder.assertSingle(InternalRoomMemberModerationEvents.DoUnbanUser(""))
    }

    @Test
    fun `clicking dismiss on unban confirmation dialog sends Reset event`() {
        val eventsRecorder = EventsRecorder<RoomMemberModerationEvents>()
        rule.setRoomMemberModerationView(
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                unbanUserAsyncAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder
            ),
        )
        rule.pressTag(TestTags.dialogNegative.value)
        eventsRecorder.assertSingle(InternalRoomMemberModerationEvents.Reset)
    }

    @Test
    fun `disabled actions are not clickable`() {
        val eventsRecorder = EventsRecorder<RoomMemberModerationEvents>(expectEvents = false)
        rule.setRoomMemberModerationView(
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                actions = listOf(
                    ModerationActionState(action = ModerationAction.KickUser, isEnabled = false),
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_bottom_sheet_manage_room_member_remove)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRoomMemberModerationView(
    state: InternalRoomMemberModerationState,
    onSelectAction: (ModerationAction, MatrixUser) -> Unit = EnsureNeverCalledWithTwoParams(),
) {
    setSafeContent {
        RoomMemberModerationView(
            state = state,
            onSelectAction = onSelectAction,
        )
    }
}
