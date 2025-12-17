/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.invite.api.InviteData
import io.element.android.features.invite.test.anInviteData
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.ui.model.toInviteSender
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JoinRoomViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invoke the expected callback`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setJoinRoomView(
                aJoinRoomState(
                    eventSink = eventsRecorder,
                ),
                onBackClick = it
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on Join room on CanJoin room emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.CanJoin),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_join_room_join_action)
        eventsRecorder.assertSingle(JoinRoomEvents.JoinRoom)
    }

    @Test
    fun `clicking on Knock room on CanKnock room emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.CanKnock),
                knockMessage = "Knock knock",
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_join_room_knock_action)
        eventsRecorder.assertSingle(JoinRoomEvents.KnockRoom)
    }

    @Test
    fun `clicking on closing Knock error emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.CanKnock),
                knockAction = AsyncAction.Failure(Exception("Error")),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(JoinRoomEvents.ClearActionStates)
    }

    @Test
    fun `clicking on cancel knock request emit the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.IsKnocked),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_join_room_cancel_knock_action)
        eventsRecorder.assertSingle(JoinRoomEvents.CancelKnock(true))
    }

    @Test
    fun `clicking on closing Cancel Knock error emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.IsKnocked),
                cancelKnockAction = AsyncAction.Failure(Exception("Error")),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(JoinRoomEvents.ClearActionStates)
    }

    @Test
    fun `clicking on closing Join error emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.CanKnock),
                joinAction = AsyncAction.Failure(Exception("Error")),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(JoinRoomEvents.ClearActionStates)
    }

    @Test
    fun `when joining room is successful, the expected callback is invoked`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setJoinRoomView(
                aJoinRoomState(
                    joinAction = AsyncAction.Success(Unit),
                    eventSink = eventsRecorder,
                ),
                onJoinSuccess = it
            )
        }
    }

    @Test
    fun `clicking on Accept when JoinAuthorisationStatus is IsInvited emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        val inviteData = anInviteData()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.IsInvited(inviteData, null)),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_accept)
        eventsRecorder.assertSingle(JoinRoomEvents.AcceptInvite(inviteData))
    }

    @Test
    fun `clicking on Decline when JoinAuthorisationStatus is IsInvited emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        val inviteData = anInviteData()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.IsInvited(inviteData, null)),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_decline)
        eventsRecorder.assertSingle(JoinRoomEvents.DeclineInvite(inviteData, false))
    }

    @Test
    fun `clicking on Decline and block when JoinAuthorisationStatus is IsInvited and can report room, the expected callback is invoked`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>(expectEvents = false)
        val inviteData = anInviteData()
        val joinRoomState = aJoinRoomState(
            contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.IsInvited(inviteData, aRoomMember().toInviteSender())),
            canReportRoom = true,
            eventSink = eventsRecorder,
        )
        ensureCalledOnceWithParam(inviteData) {
            rule.setJoinRoomView(
                state = joinRoomState,
                onDeclineInviteAndBlockUser = it,
            )
            rule.clickOn(R.string.screen_join_room_decline_and_block_button_title)
        }
    }

    @Test
    fun `clicking on Decline and block when JoinAuthorisationStatus is IsInvited and cant report room, emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        val inviteData = anInviteData()
        val joinRoomState = aJoinRoomState(
            contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.IsInvited(inviteData, aRoomMember().toInviteSender())),
            canReportRoom = false,
            eventSink = eventsRecorder,
        )
        rule.setJoinRoomView(state = joinRoomState)
        rule.clickOn(R.string.screen_join_room_decline_and_block_button_title)
        eventsRecorder.assertSingle(JoinRoomEvents.DeclineInvite(inviteData, true))
    }

    @Test
    fun `clicking on Retry when an error occurs emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aFailureContentState(),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_retry)
        eventsRecorder.assertSingle(JoinRoomEvents.RetryFetchingContent)
    }

    @Test
    fun `clicking on ok when user is unauthorized the expected callback`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setJoinRoomView(
                aJoinRoomState(
                    contentState = aLoadedContentState(),
                    joinAction = AsyncAction.Failure(JoinRoom.Failures.UnauthorizedJoin),
                    eventSink = eventsRecorder,
                ),
                onBackClick = it
            )
            rule.clickOn(CommonStrings.action_ok)
        }
    }

    @Test
    fun `clicking on forget when user is banned invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.IsBanned(null, null)),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_join_room_forget_action)
        eventsRecorder.assertSingle(JoinRoomEvents.ForgetRoom)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setJoinRoomView(
    state: JoinRoomState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onJoinSuccess: () -> Unit = EnsureNeverCalled(),
    onKnockSuccess: () -> Unit = EnsureNeverCalled(),
    onCancelKnockSuccess: () -> Unit = EnsureNeverCalled(),
    onForgetSuccess: () -> Unit = EnsureNeverCalled(),
    onDeclineInviteAndBlockUser: (InviteData) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        JoinRoomView(
            state = state,
            onBackClick = onBackClick,
            onJoinSuccess = onJoinSuccess,
            onKnockSuccess = onKnockSuccess,
            onForgetSuccess = onForgetSuccess,
            onCancelKnockSuccess = onCancelKnockSuccess,
            onDeclineInviteAndBlockUser = onDeclineInviteAndBlockUser,
        )
    }
}
