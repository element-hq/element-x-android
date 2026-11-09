/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.securebackup.impl.check

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.assertNoNodeWithText
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBackKey
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config

class SecureBackupCheckKeyViewTest : RobolectricTest() {
    @Test
    fun `back key pressed - calls onBackClick`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setSecureBackupCheckKeyView(aSecureBackupCheckKeyState(), onBackClick = callback)
            pressBackKey()
        }
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `tapping on Continue when a key is entered - submits it`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecureBackupCheckKeyEvent>()
        setSecureBackupCheckKeyView(aSecureBackupCheckKeyState(recoveryKey = "aRecoveryKey", isSubmitEnabled = true, eventSink = recorder))
        clickOn(CommonStrings.action_continue)
        recorder.assertSingle(SecureBackupCheckKeyEvent.Submit)
    }

    @Test
    fun `entering a char emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecureBackupCheckKeyEvent>()
        setSecureBackupCheckKeyView(aSecureBackupCheckKeyState(recoveryKey = "aRecoveryKey", isSubmitEnabled = true, eventSink = recorder))
        onNodeWithText("aRecoveryKey").performTextInput("X")
        recorder.assertSingle(SecureBackupCheckKeyEvent.OnRecoveryKeyChange("XaRecoveryKey"))
    }

    @Test
    fun `validating from the keyboard submits the key`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecureBackupCheckKeyEvent>()
        setSecureBackupCheckKeyView(aSecureBackupCheckKeyState(recoveryKey = "aRecoveryKey", isSubmitEnabled = true, eventSink = recorder))
        onNodeWithText("aRecoveryKey").performImeAction()
        recorder.assertSingle(SecureBackupCheckKeyEvent.Submit)
    }

    @Test
    fun `an incorrect key - is reported`() = runAndroidComposeUiTest {
        setSecureBackupCheckKeyView(aSecureBackupCheckKeyState(recoveryKey = "1234", isKeyIncorrect = true))
        onNodeWithText(activity!!.getString(R.string.screen_recovery_key_confirm_error_title)).assertExists()
    }

    @Test
    fun `a key not checked yet - is not reported as incorrect`() = runAndroidComposeUiTest {
        setSecureBackupCheckKeyView(aSecureBackupCheckKeyState(recoveryKey = "1234", isSubmitEnabled = true))
        assertNoNodeWithText(R.string.screen_recovery_key_confirm_error_title)
    }

    @Test
    fun `once the key is confirmed - the done button finishes and going back is not possible`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setSecureBackupCheckKeyView(aSecureBackupCheckKeyState(isCompleted = true), onDone = callback)
            pressBackKey()
            clickOn(CommonStrings.action_done)
        }
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setSecureBackupCheckKeyView(
        state: SecureBackupCheckKeyState,
        onDone: () -> Unit = EnsureNeverCalled(),
        onBackClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            SecureBackupCheckKeyView(
                state = state,
                doneButtonText = CommonStrings.action_done,
                onDone = onDone,
                onBackClick = onBackClick,
            )
        }
    }
}
