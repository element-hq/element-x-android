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

package io.element.android.features.roomdetails.members.moderation.changeroles

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomdetails.impl.rolesandpermissions.changeroles.ChangeRolesEvent
import io.element.android.features.roomdetails.impl.rolesandpermissions.changeroles.ChangeRolesState
import io.element.android.features.roomdetails.impl.rolesandpermissions.changeroles.ChangeRolesView
import io.element.android.features.roomdetails.impl.rolesandpermissions.changeroles.aChangeRolesState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.pressBackKey
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.lang.IllegalStateException

@RunWith(AndroidJUnit4::class)
class ChangeRolesViewTests {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `passing a 'USER' role throws an exception`() {
        val exception = runCatching {
            rule.setChangeRolesContent(
                state = aChangeRolesState(
                    role = RoomMember.Role.USER,
                    eventSink = EnsureNeverCalledWithParam(),
                ),
            )
        }.exceptionOrNull()

        assertThat(exception).isNotNull()
    }

    @Test
    fun `back key - with search active toggles the search`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        rule.setChangeRolesContent(
            state = aChangeRolesState(
                isSearchActive = true,
                eventSink = eventsRecorder,
            ),
        )

        rule.pressBackKey()

        eventsRecorder.assertSingle(ChangeRolesEvent.ToggleSearchActive)
    }

    @Test
    fun `back key - with search inactive exits the screen`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        rule.setChangeRolesContent(
            state = aChangeRolesState(
                isSearchActive = false,
                eventSink = eventsRecorder,
            ),
        )

        rule.pressBackKey()

        eventsRecorder.assertList(listOf(ChangeRolesEvent.QueryChanged(""), ChangeRolesEvent.Exit))
    }

    @Test
    fun `back button - exits the screen`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        rule.setChangeRolesContent(
            state = aChangeRolesState(
                isSearchActive = false,
                eventSink = eventsRecorder,
            ),
        )

        rule.pressBack()

        eventsRecorder.assertList(listOf(ChangeRolesEvent.QueryChanged(""), ChangeRolesEvent.Exit))
    }

    @Test
    fun `save button - with changes, it saves them`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        rule.setChangeRolesContent(
            state = aChangeRolesState(
                hasPendingChanges = true,
                eventSink = eventsRecorder,
            ),
        )

        rule.clickOn(CommonStrings.action_save)

        eventsRecorder.assertList(listOf(ChangeRolesEvent.QueryChanged(""), ChangeRolesEvent.Save))
    }

    @Test
    fun `save button - with no changes, does nothing`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        rule.setChangeRolesContent(
            state = aChangeRolesState(
                hasPendingChanges = false,
                eventSink = eventsRecorder,
            ),
        )

        rule.clickOn(CommonStrings.action_save)

        eventsRecorder.assertList(listOf(ChangeRolesEvent.QueryChanged("")))
    }

    @Test
    fun `exit confirmation dialog - submit exits the screen`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        rule.setChangeRolesContent(
            state = aChangeRolesState(
                isSearchActive = true,
                exitState = AsyncAction.Confirming,
                eventSink = eventsRecorder,
            ),
        )

        rule.clickOn(CommonStrings.action_ok)

        eventsRecorder.assertSingle(ChangeRolesEvent.Exit)
    }

    @Test
    fun `exit confirmation dialog - cancel removes the dialog`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        rule.setChangeRolesContent(
            state = aChangeRolesState(
                isSearchActive = true,
                exitState = AsyncAction.Confirming,
                eventSink = eventsRecorder,
            ),
        )

        rule.clickOn(CommonStrings.action_cancel)

        eventsRecorder.assertSingle(ChangeRolesEvent.CancelExit)
    }

    @Test
    fun `save confirmation dialog - submit saves the changes`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        rule.setChangeRolesContent(
            state = aChangeRolesState(
                role = RoomMember.Role.ADMIN,
                isSearchActive = true,
                savingState = AsyncAction.Confirming,
                eventSink = eventsRecorder,
            ),
        )

        rule.clickOn(CommonStrings.action_ok)

        eventsRecorder.assertSingle(ChangeRolesEvent.Save)
    }

    @Test
    fun `save confirmation dialog - cancel removes the dialog`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        rule.setChangeRolesContent(
            state = aChangeRolesState(
                role = RoomMember.Role.ADMIN,
                isSearchActive = true,
                savingState = AsyncAction.Confirming,
                eventSink = eventsRecorder,
            ),
        )

        rule.clickOn(CommonStrings.action_cancel)

        eventsRecorder.assertSingle(ChangeRolesEvent.ClearError)
    }

    @Test
    fun `error dialog - dismissing removes the dialog`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        rule.setChangeRolesContent(
            state = aChangeRolesState(
                isSearchActive = true,
                savingState = AsyncAction.Failure(IllegalStateException("boom")),
                eventSink = eventsRecorder,
            ),
        )

        rule.clickOn(CommonStrings.action_ok)

        eventsRecorder.assertSingle(ChangeRolesEvent.ClearError)
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setChangeRolesContent(
        state: ChangeRolesState,
        onBackPressed: () -> Unit = EnsureNeverCalled(),
    ) {
        rule.setContent {
            ChangeRolesView(
                state = state,
                navigateUp = onBackPressed,
            )
        }
    }
}
