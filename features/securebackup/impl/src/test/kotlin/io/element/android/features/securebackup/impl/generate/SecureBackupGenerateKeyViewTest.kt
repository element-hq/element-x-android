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
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
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
