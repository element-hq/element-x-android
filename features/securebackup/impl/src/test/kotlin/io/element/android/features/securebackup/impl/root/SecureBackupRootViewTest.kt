/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.securebackup.impl.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class SecureBackupRootViewTest : RobolectricTest() {
    @Test
    fun `disabled recovery with remote backup routes to enter recovery key`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setSecureBackupRootView(
                state = disabledRecoveryState(doesBackupExistOnServer = true),
                onConfirmRecoveryKeyClick = callback,
            )
            clickOn(R.string.screen_chat_backup_recovery_action_confirm)
        }
    }

    @Test
    fun `disabled recovery without remote backup presents setup recovery`() = runAndroidComposeUiTest {
        setSecureBackupRootView(state = disabledRecoveryState(doesBackupExistOnServer = false))

        onNodeWithText(activity!!.getString(R.string.screen_chat_backup_recovery_action_setup)).assertExists()
    }

    private fun disabledRecoveryState(doesBackupExistOnServer: Boolean) = aSecureBackupRootState(
        backupState = BackupState.UNKNOWN,
        doesBackupExistOnServer = AsyncData.Success(doesBackupExistOnServer),
        recoveryState = RecoveryState.DISABLED,
    )

    private fun AndroidComposeUiTest<ComponentActivity>.setSecureBackupRootView(
        state: SecureBackupRootState,
        onSetupClick: () -> Unit = EnsureNeverCalled(),
        onConfirmRecoveryKeyClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            SecureBackupRootView(
                state = state,
                onBackClick = EnsureNeverCalled(),
                onSetupClick = onSetupClick,
                onChangeClick = EnsureNeverCalled(),
                onDisableClick = EnsureNeverCalled(),
                onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
                onLearnMoreClick = EnsureNeverCalled(),
            )
        }
    }
}
