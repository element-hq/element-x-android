/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomlist.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.roomlist.impl.components.RoomListMenuAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomListViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on close verification banner emits the expected Event`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        rule.setRoomListView(
            state = aRoomListState(
                securityBannerState = SecurityBannerState.SessionVerification,
                eventSink = eventsRecorder,
            )
        )
        val close = rule.activity.getString(CommonStrings.action_close)
        rule.onNodeWithContentDescription(close).performClick()
        eventsRecorder.assertSingle(RoomListEvents.DismissRequestVerificationPrompt)
    }

    @Test
    fun `clicking on continue verification banner invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setRoomListView(
                state = aRoomListState(
                    securityBannerState = SecurityBannerState.SessionVerification,
                    eventSink = eventsRecorder,
                ),
                onVerifyClicked = callback,
            )
            rule.clickOn(CommonStrings.action_continue)
        }
    }

    @Test
    fun `clicking on close recovery key banner emits the expected Event`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        rule.setRoomListView(
            state = aRoomListState(
                securityBannerState = SecurityBannerState.RecoveryKeyConfirmation,
                eventSink = eventsRecorder,
            )
        )
        val close = rule.activity.getString(CommonStrings.action_close)
        rule.onNodeWithContentDescription(close).performClick()
        eventsRecorder.assertSingle(RoomListEvents.DismissRecoveryKeyPrompt)
    }

    @Test
    fun `clicking on continue recovery key banner invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setRoomListView(
                state = aRoomListState(
                    securityBannerState = SecurityBannerState.RecoveryKeyConfirmation,
                    eventSink = eventsRecorder,
                ),
                onConfirmRecoveryKeyClicked = callback,
            )
            rule.clickOn(CommonStrings.action_continue)
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRoomListView(
    state: RoomListState,
    onRoomClicked: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onSettingsClicked: () -> Unit = EnsureNeverCalled(),
    onVerifyClicked: () -> Unit = EnsureNeverCalled(),
    onConfirmRecoveryKeyClicked: () -> Unit = EnsureNeverCalled(),
    onCreateRoomClicked: () -> Unit = EnsureNeverCalled(),
    onInvitesClicked: () -> Unit = EnsureNeverCalled(),
    onRoomSettingsClicked: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onMenuActionClicked: (RoomListMenuAction) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        RoomListView(
            state = state,
            onRoomClicked = onRoomClicked,
            onSettingsClicked = onSettingsClicked,
            onVerifyClicked = onVerifyClicked,
            onConfirmRecoveryKeyClicked = onConfirmRecoveryKeyClicked,
            onCreateRoomClicked = onCreateRoomClicked,
            onInvitesClicked = onInvitesClicked,
            onRoomSettingsClicked = onRoomSettingsClicked,
            onMenuActionClicked = onMenuActionClicked,
        )
    }
}
