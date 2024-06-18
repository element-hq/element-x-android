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

package io.element.android.features.securebackup.impl.enter

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.securebackup.impl.R
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
    fun `tapping on Continue when key is valid - calls expected action`() {
        val recorder = EventsRecorder<SecureBackupEnterRecoveryKeyEvents>()
        rule.setSecureBackupEnterRecoveryKeyView(
            aSecureBackupEnterRecoveryKeyState(isSubmitEnabled = true, eventSink = recorder),
        )
        rule.clickOn(CommonStrings.action_continue)

        recorder.assertSingle(SecureBackupEnterRecoveryKeyEvents.Submit)
    }

    @Test
    fun `tapping on Lost your recovery key - calls onCreateNewRecoveryKey`() {
        ensureCalledOnce { callback ->
            rule.setSecureBackupEnterRecoveryKeyView(
                aSecureBackupEnterRecoveryKeyState(),
                onCreateNewRecoveryKey = callback,
            )
            rule.clickOn(R.string.screen_recovery_key_confirm_lost_recovery_key)
        }
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
        onCreateNewRecoveryKey: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            SecureBackupEnterRecoveryKeyView(
                state = state,
                onSuccess = onDone,
                onBackClick = onBackClick,
                onCreateNewRecoveryKey = onCreateNewRecoveryKey
            )
        }
    }
}
