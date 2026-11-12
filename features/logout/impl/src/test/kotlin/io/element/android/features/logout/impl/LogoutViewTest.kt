/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.logout.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class LogoutViewTest : RobolectricTest() {
    @Test
    fun `another verified device - removing sends a LogoutEvent`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvent>()
        setLogoutView(aLogoutState(confirmation = LogoutConfirmation.OtherVerifiedDevice, eventSink = eventsRecorder))
        clickOn(CommonStrings.action_remove, inDialog = true)
        eventsRecorder.assertSingle(LogoutEvent.Logout(ignoreSdkError = false))
    }

    @Test
    fun `another verified device - cancelling dismisses`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setLogoutView(
                aLogoutState(confirmation = LogoutConfirmation.OtherVerifiedDevice, eventSink = EventsRecorder(expectEvents = false)),
                onDismiss = callback,
            )
            clickOn(CommonStrings.action_cancel, inDialog = true)
        }
    }

    @Test
    fun `recovery key only - continuing removes the device`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvent>()
        setLogoutView(aLogoutState(confirmation = LogoutConfirmation.RecoveryKeyOnly, eventSink = eventsRecorder))
        clickOn(R.string.screen_signout_save_recovery_key_action_continue)
        eventsRecorder.assertSingle(LogoutEvent.Logout(ignoreSdkError = false))
    }

    @Test
    fun `recovery key only - the key can be generated`() = runAndroidComposeUiTest {
        ensureCalledOnceWithParam(true) { callback ->
            setLogoutView(
                aLogoutState(confirmation = LogoutConfirmation.RecoveryKeyOnly, eventSink = EventsRecorder(expectEvents = false)),
                onGenerateRecoveryKeyClick = callback,
            )
            clickOn(R.string.screen_signout_save_recovery_key_action_generate)
        }
    }

    @Test
    fun `recovery key only - the key can be checked`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setLogoutView(
                aLogoutState(confirmation = LogoutConfirmation.RecoveryKeyOnly, eventSink = EventsRecorder(expectEvents = false)),
                onCheckRecoveryKeyClick = callback,
            )
            clickOn(R.string.screen_signout_save_recovery_key_action_check)
        }
    }

    @Test
    fun `no recovery - the key can be generated`() = runAndroidComposeUiTest {
        ensureCalledOnceWithParam(false) { callback ->
            setLogoutView(
                aLogoutState(confirmation = LogoutConfirmation.NoRecovery, eventSink = EventsRecorder(expectEvents = false)),
                onGenerateRecoveryKeyClick = callback,
            )
            clickOn(R.string.screen_chat_backup_recovery_action_setup)
        }
    }

    @Test
    fun `no recovery - the device can still be removed`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvent>()
        setLogoutView(aLogoutState(confirmation = LogoutConfirmation.NoRecovery, eventSink = eventsRecorder))
        clickOn(CommonStrings.action_signout_anyway)
        eventsRecorder.assertSingle(LogoutEvent.Logout(ignoreSdkError = false))
    }

    @Test
    fun `no recovery - learn more opens the explanation`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setLogoutView(
                aLogoutState(confirmation = LogoutConfirmation.NoRecovery, eventSink = EventsRecorder(expectEvents = false)),
                onLearnMoreClick = callback,
            )
            clickOn(CommonStrings.action_learn_more)
        }
    }

    @Test
    fun `key backup disabled - settings can be opened and the device removed`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvent>()
        ensureCalledOnce { callback ->
            setLogoutView(
                aLogoutState(confirmation = LogoutConfirmation.KeyBackupDisabled, eventSink = eventsRecorder),
                onOpenSettingsClick = callback,
            )
            clickOn(CommonStrings.common_settings, inDialog = true)
        }
        eventsRecorder.assertEmpty()
    }

    @Test
    fun `key backup disabled - removing sends a LogoutEvent`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvent>()
        setLogoutView(aLogoutState(confirmation = LogoutConfirmation.KeyBackupDisabled, eventSink = eventsRecorder))
        clickOn(CommonStrings.action_remove, inDialog = true)
        eventsRecorder.assertSingle(LogoutEvent.Logout(ignoreSdkError = false))
    }

    @Test
    fun `keys being backed up - acknowledging dismisses`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setLogoutView(
                aLogoutState(confirmation = LogoutConfirmation.BackupOngoing, eventSink = EventsRecorder(expectEvents = false)),
                onDismiss = callback,
            )
            clickOn(CommonStrings.action_ok, inDialog = true)
        }
    }

    @Test
    fun `keys that cannot be backed up while offline - acknowledging dismisses`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setLogoutView(
                aLogoutState(confirmation = LogoutConfirmation.BackupOffline, eventSink = EventsRecorder(expectEvents = false)),
                onDismiss = callback,
            )
            clickOn(CommonStrings.action_ok, inDialog = true)
        }
    }

    @Test
    fun `failing to remove the device - removing anyway sends a LogoutEvent`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvent>()
        setLogoutView(aLogoutState(logoutAction = AsyncAction.Failure(Exception("Failed to logout")), eventSink = eventsRecorder))
        clickOn(CommonStrings.action_signout_anyway)
        eventsRecorder.assertSingle(LogoutEvent.Logout(ignoreSdkError = true))
    }

    @Test
    fun `failing to remove the device - cancelling closes the dialogs`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvent>()
        setLogoutView(aLogoutState(logoutAction = AsyncAction.Failure(Exception("Failed to logout")), eventSink = eventsRecorder))
        clickOn(CommonStrings.action_cancel, inDialog = true)
        eventsRecorder.assertSingle(LogoutEvent.CloseDialogs)
    }

    @Test
    fun `removing the device shows that it is in progress`() = runAndroidComposeUiTest {
        setLogoutView(aLogoutState(logoutAction = AsyncAction.Loading, eventSink = EventsRecorder(expectEvents = false)))
        onNodeWithText(activity!!.getString(R.string.screen_signout_in_progress_dialog_content)).assertExists()
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setLogoutView(
        state: LogoutState,
        onDismiss: () -> Unit = EnsureNeverCalled(),
        onGenerateRecoveryKeyClick: (Boolean) -> Unit = EnsureNeverCalledWithParam(),
        onCheckRecoveryKeyClick: () -> Unit = EnsureNeverCalled(),
        onOpenSettingsClick: () -> Unit = EnsureNeverCalled(),
        onLearnMoreClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            LogoutView(
                state = state,
                onDismiss = onDismiss,
                onGenerateRecoveryKeyClick = onGenerateRecoveryKeyClick,
                onCheckRecoveryKeyClick = onCheckRecoveryKeyClick,
                onOpenSettingsClick = onOpenSettingsClick,
                onLearnMoreClick = onLearnMoreClick,
            )
        }
    }
}
