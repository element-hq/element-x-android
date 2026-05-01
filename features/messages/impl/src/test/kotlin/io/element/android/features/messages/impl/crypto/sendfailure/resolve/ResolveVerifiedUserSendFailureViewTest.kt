/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.messages.impl.crypto.sendfailure.resolve

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.setSafeContent
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResolveVerifiedUserSendFailureViewTest {
    @Test
    fun `clicking on resolve and resend emit the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ResolveVerifiedUserSendFailureEvent>()
        setResolveVerifiedUserSendFailureView(
            state = aResolveVerifiedUserSendFailureState(
                verifiedUserSendFailure = aChangedIdentitySendFailure(),
                eventSink = eventsRecorder,
            ),
        )

        clickOn(res = CommonStrings.screen_resolve_send_failure_changed_identity_primary_button_title)
        eventsRecorder.assertSingle(ResolveVerifiedUserSendFailureEvent.ResolveAndResend)
    }

    @Test
    fun `clicking on retry emit the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ResolveVerifiedUserSendFailureEvent>()
        setResolveVerifiedUserSendFailureView(
            state = aResolveVerifiedUserSendFailureState(
                verifiedUserSendFailure = aChangedIdentitySendFailure(),
                eventSink = eventsRecorder,
            ),
        )

        clickOn(res = CommonStrings.action_retry)
        eventsRecorder.assertSingle(ResolveVerifiedUserSendFailureEvent.Retry)
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setResolveVerifiedUserSendFailureView(
        state: ResolveVerifiedUserSendFailureState,
    ) {
        setSafeContent {
            ResolveVerifiedUserSendFailureView(state = state)
        }
    }
}
