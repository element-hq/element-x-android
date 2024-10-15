/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
            rule.setBlockedUsersView(
                aBlockedUsersState(
                    eventSink = eventsRecorder
                ),
                onBackClick = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on a user emits the expected Event`() {
        val eventsRecorder = EventsRecorder<BlockedUsersEvents>()
        val userList = aMatrixUserList()
        rule.setBlockedUsersView(
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
        rule.setBlockedUsersView(
            aBlockedUsersState(
                unblockUserAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(BlockedUsersEvents.Cancel)
    }

    @Test
    fun `clicking on confirm sends a BlockedUsersEvents`() {
        val eventsRecorder = EventsRecorder<BlockedUsersEvents>()
        rule.setBlockedUsersView(
            aBlockedUsersState(
                unblockUserAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_blocked_users_unblock_alert_action)
        eventsRecorder.assertSingle(BlockedUsersEvents.ConfirmUnblock)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setBlockedUsersView(
    state: BlockedUsersState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        BlockedUsersView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
