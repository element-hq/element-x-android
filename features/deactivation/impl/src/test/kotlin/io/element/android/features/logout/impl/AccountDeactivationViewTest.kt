/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.logout.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.deactivation.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_PASSWORD
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.pressTag
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class AccountDeactivationViewTest {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountDeactivationEvents>(expectEvents = false)
        ensureCalledOnce {
            setAccountDeactivationView(
                state = anAccountDeactivationState(eventSink = eventsRecorder),
                onBackClick = it,
            )
            pressBack()
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on Deactivate emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountDeactivationEvents>()
        setAccountDeactivationView(
            state = anAccountDeactivationState(
                deactivateFormState = aDeactivateFormState(
                    password = A_PASSWORD,
                ),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_delete)
        eventsRecorder.assertSingle(AccountDeactivationEvents.DeactivateAccount(false))
    }

    @Test
    fun `clicking on Deactivate on the confirmation dialog emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountDeactivationEvents>()
        setAccountDeactivationView(
            state = anAccountDeactivationState(
                deactivateFormState = aDeactivateFormState(
                    password = A_PASSWORD,
                ),
                accountDeactivationAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder,
            ),
        )
        pressTag(TestTags.dialogPositive.value)
        eventsRecorder.assertSingle(AccountDeactivationEvents.DeactivateAccount(false))
    }

    @Test
    fun `clicking on retry on the confirmation dialog emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountDeactivationEvents>()
        setAccountDeactivationView(
            state = anAccountDeactivationState(
                deactivateFormState = aDeactivateFormState(
                    password = A_PASSWORD,
                ),
                accountDeactivationAction = AsyncAction.Failure(AN_EXCEPTION),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_retry)
        eventsRecorder.assertSingle(AccountDeactivationEvents.DeactivateAccount(true))
    }

    @Test
    fun `switching on the erase all switch emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountDeactivationEvents>()
        setAccountDeactivationView(
            state = anAccountDeactivationState(
                eventSink = eventsRecorder,
            ),
        )
        clickOn(R.string.screen_deactivate_account_delete_all_messages)
        eventsRecorder.assertSingle(AccountDeactivationEvents.SetEraseData(true))
    }

    @Test
    fun `switching off the erase all switch emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountDeactivationEvents>()
        setAccountDeactivationView(
            state = anAccountDeactivationState(
                deactivateFormState = aDeactivateFormState(
                    eraseData = true,
                ),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(R.string.screen_deactivate_account_delete_all_messages)
        eventsRecorder.assertSingle(AccountDeactivationEvents.SetEraseData(false))
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `typing text in the password field emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountDeactivationEvents>()
        setAccountDeactivationView(
            state = anAccountDeactivationState(
                deactivateFormState = aDeactivateFormState(
                    password = A_PASSWORD,
                ),
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithTag(TestTags.loginPassword.value).performTextInput("A")
        eventsRecorder.assertSingle(AccountDeactivationEvents.SetPassword("A$A_PASSWORD"))
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setAccountDeactivationView(
    state: AccountDeactivationState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        AccountDeactivationView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
