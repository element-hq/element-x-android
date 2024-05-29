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
