/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.securebackup.impl.generate

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.assertNoNodeWithText
import io.element.android.tests.testutils.assertNodeWithTextIsDisplayed
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBackKey
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class SecureBackupGenerateKeyViewTest : RobolectricTest() {
    @Test
    fun `back key pressed - calls onBackClick`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setSecureBackupGenerateKeyView(aSecureBackupGenerateKeyState(), onBackClick = callback)
            pressBackKey()
        }
    }

    @Test
    fun `tapping on Continue - asks to confirm that the key is saved`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecureBackupGenerateKeyEvent>()
        setSecureBackupGenerateKeyView(aSecureBackupGenerateKeyState(eventSink = recorder))
        clickOn(CommonStrings.action_continue)
        recorder.assertSingle(SecureBackupGenerateKeyEvent.Continue)
    }

    @Test
    fun `confirming that the key is saved - activates it`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecureBackupGenerateKeyEvent>()
        setSecureBackupGenerateKeyView(aSecureBackupGenerateKeyState(showSaveConfirmationDialog = true, eventSink = recorder))
        // The dialog's button has the same label as the one behind it.
        clickOn(CommonStrings.action_continue, inDialog = true)
        recorder.assertSingle(SecureBackupGenerateKeyEvent.ConfirmSaved)
    }

    @Test
    fun `cancelling the confirmation - dismisses it`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecureBackupGenerateKeyEvent>()
        setSecureBackupGenerateKeyView(aSecureBackupGenerateKeyState(showSaveConfirmationDialog = true, eventSink = recorder))
        clickOn(CommonStrings.action_cancel, inDialog = true)
        recorder.assertSingle(SecureBackupGenerateKeyEvent.DismissConfirmation)
    }

    @Test
    fun `a failure to activate the key - is reported and can be dismissed`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecureBackupGenerateKeyEvent>()
        setSecureBackupGenerateKeyView(aSecureBackupGenerateKeyState(activateAction = AsyncAction.Failure(RuntimeException("Boom")), eventSink = recorder))
        assertNodeWithTextIsDisplayed(CommonStrings.common_something_went_wrong_message)
        clickOn(CommonStrings.action_ok)
        recorder.assertSingle(SecureBackupGenerateKeyEvent.DismissError)
    }

    @Test
    fun `a network failure to activate the key - tells to check the connection`() = runAndroidComposeUiTest {
        val error = ClientException.Generic("error sending request for url", null)
        setSecureBackupGenerateKeyView(aSecureBackupGenerateKeyState(activateAction = AsyncAction.Failure(error)))
        assertNodeWithTextIsDisplayed(CommonStrings.common_please_check_internet_connection)
    }

    @Test
    fun `tapping on the key - copies it`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecureBackupGenerateKeyEvent>()
        val state = aSecureBackupGenerateKeyState(eventSink = recorder)
        setSecureBackupGenerateKeyView(state)
        onNodeWithContentDescription(activity!!.getString(CommonStrings.action_copy)).performScrollTo().performClick()
        recorder.assertSingle(SecureBackupGenerateKeyEvent.CopyRecoveryKey)
    }

    @Test
    fun `the first key says where to change it later, replacing a key does not`() = runAndroidComposeUiTest {
        setSecureBackupGenerateKeyView(aSecureBackupGenerateKeyState(isReplacingExistingKey = false, activateAction = AsyncAction.Success(Unit)))
        assertNodeWithTextIsDisplayed(R.string.screen_custom_recovery_key_success_notice)
    }

    @Test
    fun `replacing a key - does not repeat where to change it`() = runAndroidComposeUiTest {
        setSecureBackupGenerateKeyView(aSecureBackupGenerateKeyState(isReplacingExistingKey = true, activateAction = AsyncAction.Success(Unit)))
        assertNoNodeWithText(R.string.screen_custom_recovery_key_success_notice)
    }

    @Test
    fun `once the key is active - the done button finishes and going back is not possible`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setSecureBackupGenerateKeyView(
                aSecureBackupGenerateKeyState(activateAction = AsyncAction.Success(Unit)),
                onDone = callback,
            )
            // Neither the back key nor the screen behind it can undo the activation.
            pressBackKey()
            clickOn(CommonStrings.action_done)
        }
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setSecureBackupGenerateKeyView(
        state: SecureBackupGenerateKeyState,
        onDone: () -> Unit = EnsureNeverCalled(),
        onBackClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            SecureBackupGenerateKeyView(
                state = state,
                doneButtonText = CommonStrings.action_done,
                onDone = onDone,
                onBackClick = onBackClick,
            )
        }
    }
}
