/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.securebackup.impl.reset.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.pressBackKey
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config

class ResetIdentityRootViewTest : RobolectricTest() {
    @Test
    fun `pressing the back HW button invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce {
            setResetRootView(
                aResetIdentityRootState(),
                onBack = it,
            )
            pressBackKey()
        }
    }

    @Test
    fun `clicking on the back navigation button invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce {
            setResetRootView(
                aResetIdentityRootState(),
                onBack = it,
            )
            pressBack()
        }
    }

    @Test
    @Config(qualifiers = "h720dp")
    fun `clicking Continue displays the confirmation dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ResetIdentityRootEvent>()
        setResetRootView(
            aResetIdentityRootState(eventSink = eventsRecorder),
        )

        clickOn(R.string.screen_encryption_reset_action_continue_reset)

        eventsRecorder.assertSingle(ResetIdentityRootEvent.Continue)
    }

    @Test
    fun `clicking 'Yes, reset now' confirms the reset`() = runAndroidComposeUiTest {
        ensureCalledOnce {
            setResetRootView(
                aResetIdentityRootState(displayConfirmationDialog = true),
                onContinue = it,
            )
            clickOn(R.string.screen_reset_encryption_confirmation_alert_action)
        }
    }

    @Test
    fun `clicking Cancel dismisses the dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ResetIdentityRootEvent>()
        setResetRootView(
            aResetIdentityRootState(displayConfirmationDialog = true, eventSink = eventsRecorder),
        )

        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ResetIdentityRootEvent.DismissDialog)
    }

    @Test
    @Config(qualifiers = "h720dp")
    fun `without other options, clicking Continue resets without asking for confirmation`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ResetIdentityRootEvent>(expectEvents = false)
        ensureCalledOnce {
            setResetRootView(
                aResetIdentityRootState(isResetTheOnlyOption = true, hasEncryptedRooms = AsyncData.Success(true), eventSink = eventsRecorder),
                onContinue = it,
            )
            clickOn(R.string.screen_encryption_reset_action_continue_reset)
        }
    }

    @Test
    @Config(qualifiers = "h720dp")
    fun `without other options, clicking Sign out invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce {
            setResetRootView(
                aResetIdentityRootState(isResetTheOnlyOption = true, hasEncryptedRooms = AsyncData.Success(false)),
                onSignOut = it,
            )
            clickOn(R.string.screen_identity_confirmation_sign_out)
        }
    }

    @Test
    fun `without other options, pressing the back HW button does not navigate back`() = runAndroidComposeUiTest {
        setResetRootView(aResetIdentityRootState(isResetTheOnlyOption = true, hasEncryptedRooms = AsyncData.Success(true)))

        pressBackKey()
    }

    @Test
    fun `without other options, the back navigation button is not displayed`() = runAndroidComposeUiTest {
        setResetRootView(aResetIdentityRootState(isResetTheOnlyOption = true, hasEncryptedRooms = AsyncData.Success(true)))

        onNodeWithContentDescription(activity!!.getString(CommonStrings.action_back)).assertDoesNotExist()
    }

    @Test
    fun `with other options, Sign out is not displayed`() = runAndroidComposeUiTest {
        setResetRootView(aResetIdentityRootState())

        onNodeWithText(activity!!.getString(R.string.screen_identity_confirmation_sign_out)).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "h720dp")
    fun `without other options, the layout is shown and Continue is disabled while checking for encrypted chats`() = runAndroidComposeUiTest {
        setResetRootView(aResetIdentityRootState(isResetTheOnlyOption = true, hasEncryptedRooms = AsyncData.Loading()))

        val context = activity!!
        onNodeWithText(context.getString(R.string.screen_encryption_reset_no_options_title)).assertExists()
        onNodeWithText(context.getString(R.string.screen_identity_confirmation_sign_out)).assertIsEnabled()
        onNodeWithText(context.getString(R.string.screen_encryption_reset_action_continue_reset)).assertIsNotEnabled()
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setResetRootView(
    state: ResetIdentityRootState,
    onBack: () -> Unit = EnsureNeverCalled(),
    onContinue: () -> Unit = EnsureNeverCalled(),
    onSignOut: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        ResetIdentityRootView(state = state, onContinue = onContinue, onSignOut = onSignOut, onBack = onBack)
    }
}
