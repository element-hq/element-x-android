/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.securebackup.impl.enter

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class SecureBackupEnterRecoveryKeyViewTest {
    @Test
    fun `back key pressed - calls onBackClick`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setSecureBackupEnterRecoveryKeyView(
                aSecureBackupEnterRecoveryKeyState(),
                onBackClick = callback,
            )
            pressBackKey()
        }
    }

    @Test
    fun `back button clicked - calls onBackClick`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setSecureBackupEnterRecoveryKeyView(
                aSecureBackupEnterRecoveryKeyState(),
                onBackClick = callback,
            )
            pressBack()
        }
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `tapping on Continue when key is valid - calls expected action`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecureBackupEnterRecoveryKeyEvents>()
        setSecureBackupEnterRecoveryKeyView(
            aSecureBackupEnterRecoveryKeyState(isSubmitEnabled = true, eventSink = recorder),
        )
        clickOn(CommonStrings.action_continue)

        recorder.assertSingle(SecureBackupEnterRecoveryKeyEvents.Submit)
    }

    @Test
    fun `entering a char emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecureBackupEnterRecoveryKeyEvents>()
        val keyValue = aFormattedRecoveryKey()
        setSecureBackupEnterRecoveryKeyView(
            aSecureBackupEnterRecoveryKeyState(isSubmitEnabled = true, eventSink = recorder),
        )
        onNodeWithText(keyValue).performTextInput("X")
        recorder.assertSingle(
            SecureBackupEnterRecoveryKeyEvents.OnRecoveryKeyChange("X$keyValue")
        )
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `toggling the visibility of the textfield changes it`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecureBackupEnterRecoveryKeyEvents>()
        val keyValue = aFormattedRecoveryKey()
        setSecureBackupEnterRecoveryKeyView(aSecureBackupEnterRecoveryKeyState(isSubmitEnabled = true, eventSink = recorder))

        // Initially, the text field should be visible
        onNodeWithText(keyValue).assertExists()

        onNodeWithContentDescription(activity!!.getString(CommonStrings.a11y_hide_password)).performClick()

        waitForIdle()

        recorder.assertSingle(SecureBackupEnterRecoveryKeyEvents.ChangeRecoveryKeyFieldContentsVisibility(false))
    }

    @Test
    fun `validating from keyboard emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecureBackupEnterRecoveryKeyEvents>()
        val keyValue = aFormattedRecoveryKey()
        setSecureBackupEnterRecoveryKeyView(
            aSecureBackupEnterRecoveryKeyState(isSubmitEnabled = true, eventSink = recorder),
        )
        onNodeWithText(keyValue).performImeAction()
        recorder.assertSingle(SecureBackupEnterRecoveryKeyEvents.Submit)
    }

    @Test
    fun `when submit action succeeds - calls onDone`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setSecureBackupEnterRecoveryKeyView(
                aSecureBackupEnterRecoveryKeyState(submitAction = AsyncAction.Success(Unit)),
                onDone = callback,
            )
        }
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setSecureBackupEnterRecoveryKeyView(
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
