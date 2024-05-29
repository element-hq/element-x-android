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

package io.element.android.features.preferences.impl.blockedusers

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlockedUserViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes back callback`() {
        val eventsRecorder = EventsRecorder<BlockedUsersEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setLogoutView(
                aBlockedUsersState(
                    eventSink = eventsRecorder
                ),
                onBackClicked = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on a user emits the expected Event`() {
        val eventsRecorder = EventsRecorder<BlockedUsersEvents>()
        val userList = aMatrixUserList()
        rule.setLogoutView(
            aBlockedUsersState(
                blockedUsers = userList,
                eventSink = eventsRecorder
            ),
        )
        rule.onNodeWithText(userList.first().displayName.orEmpty()).performClick()
        eventsRecorder.assertSingle(BlockedUsersEvents.Unblock(userList.first().userId))
    }

    @Test
    fun `clicking on cancel sends a BlockedUsersEvents`() {
        val eventsRecorder = EventsRecorder<BlockedUsersEvents>()
        rule.setLogoutView(
            aBlockedUsersState(
                unblockUserAction = AsyncAction.Confirming,
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(BlockedUsersEvents.Cancel)
    }

    @Test
    fun `clicking on confirm sends a BlockedUsersEvents`() {
        val eventsRecorder = EventsRecorder<BlockedUsersEvents>()
        rule.setLogoutView(
            aBlockedUsersState(
                unblockUserAction = AsyncAction.Confirming,
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_blocked_users_unblock_alert_action)
        eventsRecorder.assertSingle(BlockedUsersEvents.ConfirmUnblock)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setLogoutView(
    state: BlockedUsersState,
    onBackClicked: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        BlockedUsersView(
            state = state,
            onBackPressed = onBackClicked,
        )
    }
}
