/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.user.editprofile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.ui.media.AvatarAction
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
class EditUserProfileViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back emits the expected event`() {
        val eventsRecorder = EventsRecorder<EditUserProfileEvents>()
        rule.setEditUserProfileView(
            aEditUserProfileState(
                eventSink = eventsRecorder,
            ),
        )
        rule.pressBack()
        eventsRecorder.assertSingle(EditUserProfileEvents.Exit)
    }

    @Test
    fun `clicking on cancel exit emits the expected event`() {
        val eventsRecorder = EventsRecorder<EditUserProfileEvents>()
        rule.setEditUserProfileView(
            aEditUserProfileState(
                saveAction = AsyncAction.ConfirmingCancellation,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(EditUserProfileEvents.CloseDialog)
    }

    @Test
    fun `clicking on OK exit emits the expected event`() {
        val eventsRecorder = EventsRecorder<EditUserProfileEvents>()
        rule.setEditUserProfileView(
            aEditUserProfileState(
                saveAction = AsyncAction.ConfirmingCancellation,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(EditUserProfileEvents.Exit)
    }

    @Test
    fun `clicking on save emits the expected event`() {
        val eventsRecorder = EventsRecorder<EditUserProfileEvents>()
        rule.setEditUserProfileView(
            aEditUserProfileState(
                saveButtonEnabled = true,
                saveAction = AsyncAction.Uninitialized,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_save)
        eventsRecorder.assertSingle(EditUserProfileEvents.Save)
    }

    @Test
    fun `clicking on avatar opens the bottom sheet dialog`() {
        val eventsRecorder = EventsRecorder<EditUserProfileEvents>()
        val actions = listOf(
            AvatarAction.TakePhoto,
            AvatarAction.ChoosePhoto,
            AvatarAction.Remove,
        )
        rule.setEditUserProfileView(
            aEditUserProfileState(
                saveAction = AsyncAction.Uninitialized,
                avatarActions = actions,
                eventSink = eventsRecorder,
            ),
        )
        val contentDescription = rule.activity.getString(CommonStrings.a11y_avatar)
        rule.onNodeWithContentDescription(contentDescription).performClick()
        // Assert that the actions are displayed
        actions.forEach { action ->
            val text = rule.activity.getString(action.titleResId)
            rule.onNodeWithText(text).assertExists()
        }
    }

    @Test
    fun `success invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<EditUserProfileEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setEditUserProfileView(
                aEditUserProfileState(
                    saveAction = AsyncAction.Success(Unit),
                    eventSink = eventsRecorder,
                ),
                onEditProfileSuccess = callback,
            )
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setEditUserProfileView(
    state: EditUserProfileState,
    onEditProfileSuccess: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        EditUserProfileView(
            state = state,
            onEditProfileSuccess = onEditProfileSuccess,
        )
    }
}
