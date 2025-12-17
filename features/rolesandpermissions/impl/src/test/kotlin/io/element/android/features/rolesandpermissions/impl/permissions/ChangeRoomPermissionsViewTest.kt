/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.permissions

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.rolesandpermissions.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.pressBackKey
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangeRoomPermissionsViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `click on back icon invokes Exit`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                eventSink = recorder
            )
        )
        rule.pressBack()
        recorder.assertSingle(ChangeRoomPermissionsEvent.Exit)
    }

    @Test
    fun `click on back key invokes Exit`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                eventSink = recorder
            )
        )
        rule.pressBackKey()
        recorder.assertSingle(ChangeRoomPermissionsEvent.Exit)
    }

    @Test
    fun `when confirming exit with pending changes, using the back key actually exits`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                hasChanges = true,
                eventSink = recorder,
            ),
        )
        rule.pressBackKey()
        recorder.assertSingle(ChangeRoomPermissionsEvent.Exit)
    }

    @Test
    fun `when confirming exit with pending changes, clicking on 'discard' button in the dialog actually exits`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                hasChanges = true,
                saveAction = AsyncAction.ConfirmingCancellation,
                eventSink = recorder,
            ),
        )
        rule.clickOn(CommonStrings.action_discard)
        recorder.assertSingle(ChangeRoomPermissionsEvent.Exit)
    }

    @Test
    fun `when confirming exit with pending changes, clicking on 'save' button in the dialog saves the changes`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                hasChanges = true,
                saveAction = AsyncAction.ConfirmingCancellation,
                eventSink = recorder,
            ),
        )
        rule.clickOn(CommonStrings.action_save, inDialog = true)
        recorder.assertSingle(ChangeRoomPermissionsEvent.Save)
    }

    @Test
    fun `click on a role item triggers ChangeRole event`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                itemsBySection = persistentMapOf(
                    // Makes sure there is only one item to click on
                    RoomPermissionsSection.EditDetails to persistentListOf(RoomPermissionType.ROOM_NAME)
                ),
                eventSink = recorder,
            )
        )
        rule.clickOn(R.string.screen_room_change_permissions_room_name)
        rule.clickOn(R.string.screen_room_change_permissions_everyone)
        recorder.assertSingle(
            ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, SelectableRole.Everyone),
        )
    }

    @Test
    fun `click on the Save menu item triggers Save event`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                hasChanges = true,
                eventSink = recorder,
            ),
        )
        rule.clickOn(CommonStrings.action_save)
        recorder.assertSingle(ChangeRoomPermissionsEvent.Save)
    }

    @Test
    fun `a successful save exits the screen`() {
        ensureCalledOnceWithParam(true) { callback ->
            rule.setChangeRoomPermissionsRule(
                state = aChangeRoomPermissionsState(
                    hasChanges = true,
                    saveAction = AsyncAction.Success(true),
                ),
                onComplete = callback,
            )
            rule.clickOn(CommonStrings.action_save)
        }
    }

    @Test
    fun `a cancellation exits the screen`() {
        ensureCalledOnceWithParam(false) { callback ->
            rule.setChangeRoomPermissionsRule(
                state = aChangeRoomPermissionsState(
                    hasChanges = true,
                    saveAction = AsyncAction.Success(false),
                ),
                onComplete = callback,
            )
            rule.clickOn(CommonStrings.action_save)
        }
    }

    @Test
    fun `click on the Ok option in save error dialog triggers ResetPendingAction event`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                hasChanges = true,
                saveAction = AsyncAction.Failure(IllegalStateException("Failed to set room power levels")),
                eventSink = recorder,
            ),
        )
        rule.clickOn(CommonStrings.action_ok)
        recorder.assertSingle(ChangeRoomPermissionsEvent.ResetPendingActions)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setChangeRoomPermissionsRule(
    state: ChangeRoomPermissionsState = aChangeRoomPermissionsState(),
    onComplete: (Boolean) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        ChangeRoomPermissionsView(
            state = state,
            onComplete = onComplete,
        )
    }
}
