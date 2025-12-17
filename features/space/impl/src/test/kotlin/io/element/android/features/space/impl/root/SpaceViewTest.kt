/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.root

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_ROOM_TOPIC
import io.element.android.libraries.previewutils.room.aSpaceRoom
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
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class SpaceViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<SpaceEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setSpaceView(
                aSpaceState(
                    hasMoreToLoad = false,
                    eventSink = eventsRecorder,
                ),
                onBackClick = it,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on a room name invokes the expected callback`() {
        val aSpaceRoom = aSpaceRoom(roomId = A_ROOM_ID, displayName = A_ROOM_NAME)
        val eventsRecorder = EventsRecorder<SpaceEvents>(expectEvents = false)
        ensureCalledOnceWithParam(aSpaceRoom) {
            rule.setSpaceView(
                aSpaceState(
                    children = listOf(aSpaceRoom),
                    hasMoreToLoad = false,
                    eventSink = eventsRecorder,
                ),
                onRoomClick = it,
            )
            rule.onNodeWithText(A_ROOM_NAME).performClick()
        }
    }

    @Test
    fun `clicking on Join room emits the expected Event`() {
        val aSpaceRoom = aSpaceRoom(roomId = A_ROOM_ID, state = null)
        val eventsRecorder = EventsRecorder<SpaceEvents>()
        rule.setSpaceView(
            aSpaceState(
                children = listOf(aSpaceRoom),
                hasMoreToLoad = false,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_join)
        eventsRecorder.assertSingle(SpaceEvents.Join(aSpaceRoom))
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on accept invite emits the expected Event`() {
        val aSpaceRoom = aSpaceRoom(roomId = A_ROOM_ID, state = CurrentUserMembership.INVITED)
        val eventsRecorder = EventsRecorder<SpaceEvents>()
        rule.setSpaceView(
            aSpaceState(
                hasMoreToLoad = false,
                children = listOf(aSpaceRoom),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_accept)
        eventsRecorder.assertSingle(SpaceEvents.AcceptInvite(aSpaceRoom))
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on decline invite emits the expected Event`() {
        val aSpaceRoom = aSpaceRoom(roomId = A_ROOM_ID, state = CurrentUserMembership.INVITED)
        val eventsRecorder = EventsRecorder<SpaceEvents>()
        rule.setSpaceView(
            aSpaceState(
                hasMoreToLoad = false,
                children = listOf(aSpaceRoom),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_decline)
        eventsRecorder.assertSingle(SpaceEvents.DeclineInvite(aSpaceRoom))
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on topic emits the expected Event`() {
        val eventsRecorder = EventsRecorder<SpaceEvents>()
        rule.setSpaceView(
            aSpaceState(
                parentSpace = aSpaceRoom(topic = A_ROOM_TOPIC),
                hasMoreToLoad = false,
                eventSink = eventsRecorder,
            )
        )
        rule.onNodeWithText(A_ROOM_TOPIC).performClick()
        eventsRecorder.assertSingle(SpaceEvents.ShowTopicViewer(A_ROOM_TOPIC))
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setSpaceView(
    state: SpaceState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onRoomClick: (SpaceRoom) -> Unit = EnsureNeverCalledWithParam(),
    onShareSpace: () -> Unit = EnsureNeverCalled(),
    onLeaveSpaceClick: () -> Unit = EnsureNeverCalled(),
    onDetailsClick: () -> Unit = EnsureNeverCalled(),
    onViewMembersClick: () -> Unit = EnsureNeverCalled(),
    acceptDeclineInviteView: @Composable () -> Unit = {},
) {
    setContent {
        SpaceView(
            state = state,
            onBackClick = onBackClick,
            onRoomClick = onRoomClick,
            onShareSpace = onShareSpace,
            onLeaveSpaceClick = onLeaveSpaceClick,
            onDetailsClick = onDetailsClick,
            onViewMembersClick = onViewMembersClick,
            acceptDeclineInviteView = acceptDeclineInviteView,
        )
    }
}
