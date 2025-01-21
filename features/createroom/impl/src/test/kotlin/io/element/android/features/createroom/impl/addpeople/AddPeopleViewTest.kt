/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.addpeople

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.createroom.impl.userlist.UserListEvents
import io.element.android.features.createroom.impl.userlist.UserListState
import io.element.android.features.createroom.impl.userlist.aUserListState
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
class AddPeopleViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<UserListEvents>()
        ensureCalledOnce {
            rule.setAddPeopleView(
                aUserListState(
                    eventSink = eventsRecorder,
                ),
                onBackClick = it
            )
            rule.pressBack()
        }
        eventsRecorder.assertSingle(UserListEvents.UpdateSearchQuery(""))
    }

    @Test
    fun `clicking on back during search emits the expected Event`() {
        val eventsRecorder = EventsRecorder<UserListEvents>()
        rule.setAddPeopleView(
            aUserListState(
                isSearchActive = true,
                eventSink = eventsRecorder,
            ),
        )
        rule.pressBack()
        eventsRecorder.assertSingle(UserListEvents.OnSearchActiveChanged(false))
    }

    @Test
    fun `clicking on skip invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<UserListEvents>()
        ensureCalledOnce {
            rule.setAddPeopleView(
                aUserListState(
                    eventSink = eventsRecorder,
                ),
                onNextClick = it
            )
            rule.clickOn(CommonStrings.action_skip)
        }
        eventsRecorder.assertSingle(UserListEvents.UpdateSearchQuery(""))
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setAddPeopleView(
    state: UserListState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onNextClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        AddPeopleView(
            state = state,
            onBackClick = onBackClick,
            onNextClick = onNextClick,
        )
    }
}
