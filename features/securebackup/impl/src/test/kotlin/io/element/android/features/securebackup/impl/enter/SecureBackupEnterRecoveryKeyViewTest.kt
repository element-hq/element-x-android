/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.enter

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.securebackup.impl.setup.views.aFormattedRecoveryKey
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.pressBackKey
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class SecureBackupEnterRecoveryKeyViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `back key pressed - calls onBackClick`() {
        ensureCalledOnce { callback ->
            rule.setSecureBackupEnterRecoveryKeyView(
                aSecureBackupEnterRecoveryKeyState(),
                onBackClick = callback,
            )
            rule.pressBackKey()
        }
    }

    @Test
    fun `back button clicked - calls onBackClick`() {
        ensureCalledOnce { callback ->
            rule.setSecureBackupEnterRecoveryKeyView(
                aSecureBackupEnterRecoveryKeyState(),
                onBackClick = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `tapping on Continue when key is valid - calls expected action`() {
        val recorder = EventsRecorder<SecureBackupEnterRecoveryKeyEvents>()
        rule.setSecureBackupEnterRecoveryKeyView(
            aSecureBackupEnterRecoveryKeyState(isSubmitEnabled = true, eventSink = recorder),
        )
        rule.clickOn(CommonStrings.action_continue)

        recorder.assertSingle(SecureBackupEnterRecoveryKeyEvents.Submit)
    }

    @Test
    fun `entering a char emits the expected event`() {
        val recorder = EventsRecorder<SecureBackupEnterRecoveryKeyEvents>()
        val keyValue = aFormattedRecoveryKey()
        rule.setSecureBackupEnterRecoveryKeyView(
            aSecureBackupEnterRecoveryKeyState(isSubmitEnabled = true, eventSink = recorder),
        )
        rule.onNodeWithText(keyValue).performTextInput("X")
        recorder.assertSingle(
            SecureBackupEnterRecoveryKeyEvents.OnRecoveryKeyChange("X$keyValue")
        )
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `toggling the visibility of the textfield changes it`() {
        val recorder = EventsRecorder<SecureBackupEnterRecoveryKeyEvents>()
        val keyValue = aFormattedRecoveryKey()
        rule.setSecureBackupEnterRecoveryKeyView(aSecureBackupEnterRecoveryKeyState(isSubmitEnabled = true, eventSink = recorder))

        // Initially, the text field should be visible
        rule.onNodeWithText(keyValue).assertExists()

        rule.onNodeWithContentDescription(rule.activity.getString(CommonStrings.a11y_hide_password)).performClick()

        rule.waitForIdle()

        recorder.assertSingle(SecureBackupEnterRecoveryKeyEvents.ChangeRecoveryKeyFieldContentsVisibility(false))
    }

    @Test
    fun `validating from keyboard emits the expected event`() {
        val recorder = EventsRecorder<SecureBackupEnterRecoveryKeyEvents>()
        val keyValue = aFormattedRecoveryKey()
        rule.setSecureBackupEnterRecoveryKeyView(
            aSecureBackupEnterRecoveryKeyState(isSubmitEnabled = true, eventSink = recorder),
        )
        rule.onNodeWithText(keyValue).performImeAction()
        recorder.assertSingle(SecureBackupEnterRecoveryKeyEvents.Submit)
    }

    @Test
    fun `when submit action succeeds - calls onDone`() {
        ensureCalledOnce { callback ->
            rule.setSecureBackupEnterRecoveryKeyView(
                aSecureBackupEnterRecoveryKeyState(submitAction = AsyncAction.Success(Unit)),
                onDone = callback,
            )
        }
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setSecureBackupEnterRecoveryKeyView(
        state: SecureBackupEnterRecoveryKeyState,
        onDone: () -> Unit = EnsureNeverCalled(),
        onBackClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            SecureBackupEnterRecoveryKeyView(
                state = state,
                onSuccess = onDone,
                onBackClick = onBackClick,
            )
        }
    }
}
