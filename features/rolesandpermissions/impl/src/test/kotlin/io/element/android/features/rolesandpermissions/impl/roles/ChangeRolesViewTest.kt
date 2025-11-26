/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.roles

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.toMatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.pressBackKey
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class ChangeRolesViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `passing a 'User' role throws an exception`() {
        val exception = runCatchingExceptions {
            rule.setChangeRolesContent(
                state = aChangeRolesState(
                    role = RoomMember.Role.User,
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
                savingState = AsyncAction.ConfirmingCancellation,
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
                savingState = AsyncAction.ConfirmingCancellation,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ChangeRolesEvent.CloseDialog)
    }

    @Test
    fun `save admins confirmation dialog - submit saves the changes`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        rule.setChangeRolesContent(
            state = aChangeRolesState(
                role = RoomMember.Role.Admin,
                isSearchActive = true,
                savingState = ConfirmingModifyingAdmins,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(ChangeRolesEvent.Save)
    }

    @Test
    fun `save owners confirmation dialog - continue saves the changes`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        rule.setChangeRolesContent(
            state = aChangeRolesState(
                role = RoomMember.Role.Owner(isCreator = false),
                isSearchActive = true,
                savingState = ConfirmingModifyingOwners,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(ChangeRolesEvent.Save)
    }

    @Test
    fun `save admins confirmation dialog - cancel removes the dialog`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        rule.setChangeRolesContent(
            state = aChangeRolesState(
                role = RoomMember.Role.Admin,
                isSearchActive = true,
                savingState = ConfirmingModifyingAdmins,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ChangeRolesEvent.CloseDialog)
    }

    @Test
    fun `save owners confirmation dialog - cancel removes the dialog`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        rule.setChangeRolesContent(
            state = aChangeRolesState(
                role = RoomMember.Role.Owner(isCreator = false),
                isSearchActive = true,
                savingState = ConfirmingModifyingOwners,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ChangeRolesEvent.CloseDialog)
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
        eventsRecorder.assertSingle(ChangeRolesEvent.CloseDialog)
    }

    @Test
    fun `testing removing user from selected list emits the expected event`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        val selectedUsers = aMatrixUserList().take(2)
        val userToDeselect = selectedUsers[1]
        assertThat(userToDeselect.displayName).isEqualTo("Bob")
        rule.setChangeRolesContent(
            state = aChangeRolesStateWithSelectedUsers().copy(
                selectedUsers = selectedUsers.toImmutableList(),
                eventSink = eventsRecorder,
            ),
        )
        // Unselect the user from the row list
        val contentDescription = rule.activity.getString(CommonStrings.action_remove)
        rule.onNodeWithContentDescription(
            label = contentDescription,
            useUnmergedTree = true,
        ).performClick()
        eventsRecorder.assertList(
            listOf(
                ChangeRolesEvent.QueryChanged(""),
                ChangeRolesEvent.UserSelectionToggled(userToDeselect),
            )
        )
    }

    @Test
    @Config(qualifiers = "h1000dp")
    fun `testing adding user to the selected list emits the expected event`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        val selectedUsers = aMatrixUserList().take(2)
        val state = aChangeRolesStateWithSelectedUsers().copy(
            selectedUsers = selectedUsers.toImmutableList(),
            eventSink = eventsRecorder,
        )
        val userToSelect = (state.searchResults as SearchBarResultState.Results).results.members.first().toMatrixUser()
        assertThat(userToSelect.displayName).isEqualTo("Carol")
        rule.setChangeRolesContent(
            state = state,
        )
        // Select the user from the user list
        rule.onNodeWithText("Carol").performClick()
        eventsRecorder.assertList(
            listOf(
                ChangeRolesEvent.QueryChanged(""),
                ChangeRolesEvent.UserSelectionToggled(userToSelect),
            )
        )
    }

    @Test
    fun `testing removing user to the selected list emits the expected event`() {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        val selectedUsers = aMatrixUserList().take(2)
        val state = aChangeRolesStateWithSelectedUsers().copy(
            selectedUsers = selectedUsers.toImmutableList(),
            eventSink = eventsRecorder,
        )
        val userToSelect = (state.searchResults as SearchBarResultState.Results).results.moderators.first().toMatrixUser()
        assertThat(userToSelect.displayName).isEqualTo("Bob")
        rule.setChangeRolesContent(
            state = state,
        )
        // Unselect the user from the user list
        rule.onAllNodesWithText(
            text = "Bob",
            useUnmergedTree = true,
        )[1].performClick()
        eventsRecorder.assertList(
            listOf(
                ChangeRolesEvent.QueryChanged(""),
                ChangeRolesEvent.UserSelectionToggled(userToSelect),
            )
        )
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setChangeRolesContent(
        state: ChangeRolesState,
    ) {
        setContent {
            ChangeRolesView(
                state = state,
            )
        }
    }
}
