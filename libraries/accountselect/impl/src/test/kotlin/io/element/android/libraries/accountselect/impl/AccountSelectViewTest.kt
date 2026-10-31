/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.libraries.accountselect.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.components.switchaccount.anOtherAccount
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config

class AccountSelectViewTest : RobolectricTest() {
    @Test
    fun `clicking on continue selects the current account`() = runAndroidComposeUiTest {
        ensureCalledOnceWithParam<SessionId>(UserId("@alice:example.org")) { callback ->
            setAccountSelectView(
                aTwoAccountsState(),
                onSelectAccount = callback,
            )
            clickOn(CommonStrings.action_continue)
        }
    }

    @Test
    fun `clicking on switch account emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountSelectEvent>()
        setAccountSelectView(
            aTwoAccountsState(eventSink = eventsRecorder),
        )
        clickOn(CommonStrings.common_switch_account)
        eventsRecorder.assertSingle(AccountSelectEvent.ToggleAccountListExpanded)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on another account emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountSelectEvent>()
        setAccountSelectView(
            aTwoAccountsState(eventSink = eventsRecorder),
        )
        onNodeWithText("Bob").performClick()
        eventsRecorder.assertSingle(AccountSelectEvent.SelectAccount(UserId("@bob:example.org")))
    }

    @Test
    fun `the explanation is displayed for a room link`() = runAndroidComposeUiTest {
        setAccountSelectView(aTwoAccountsState(isRoomLink = true))
        onNodeWithText(activity!!.getString(R.string.screen_select_account_permalink_room_subtitle)).assertExists()
    }

    @Test
    fun `the explanation is not displayed for a user link`() = runAndroidComposeUiTest {
        setAccountSelectView(aTwoAccountsState(isRoomLink = false))
        onNodeWithText(activity!!.getString(R.string.screen_select_account_permalink_room_subtitle)).assertDoesNotExist()
    }

    @Test
    fun `pressing back invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setAccountSelectView(
                aTwoAccountsState(),
                onDismiss = callback,
            )
            pressBack()
        }
    }
}

private fun aTwoAccountsState(
    isRoomLink: Boolean = false,
    eventSink: (AccountSelectEvent) -> Unit = {},
) = anAccountSelectState(
    isRoomLink = isRoomLink,
    currentAccount = aMatrixUser(id = "@alice:example.org", displayName = "Alice"),
    otherAccounts = listOf(anOtherAccount(aMatrixUser(id = "@bob:example.org", displayName = "Bob"))),
    eventSink = eventSink,
)

private fun AndroidComposeUiTest<ComponentActivity>.setAccountSelectView(
    state: AccountSelectState,
    onSelectAccount: (SessionId) -> Unit = EnsureNeverCalledWithParam(),
    onDismiss: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        AccountSelectView(
            state = state,
            onSelectAccount = onSelectAccount,
            onDismiss = onDismiss,
        )
    }
}
