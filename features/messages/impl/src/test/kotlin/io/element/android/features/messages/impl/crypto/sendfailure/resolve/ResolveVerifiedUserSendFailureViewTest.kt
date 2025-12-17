/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.sendfailure.resolve

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.setSafeContent
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResolveVerifiedUserSendFailureViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on resolve and resend emit the expected event`() {
        val eventsRecorder = EventsRecorder<ResolveVerifiedUserSendFailureEvents>()
        rule.setResolveVerifiedUserSendFailureView(
            state = aResolveVerifiedUserSendFailureState(
                verifiedUserSendFailure = aChangedIdentitySendFailure(),
                eventSink = eventsRecorder,
            ),
        )

        rule.clickOn(res = CommonStrings.screen_resolve_send_failure_changed_identity_primary_button_title)
        eventsRecorder.assertSingle(ResolveVerifiedUserSendFailureEvents.ResolveAndResend)
    }

    @Test
    fun `clicking on retry emit the expected event`() {
        val eventsRecorder = EventsRecorder<ResolveVerifiedUserSendFailureEvents>()
        rule.setResolveVerifiedUserSendFailureView(
            state = aResolveVerifiedUserSendFailureState(
                verifiedUserSendFailure = aChangedIdentitySendFailure(),
                eventSink = eventsRecorder,
            ),
        )

        rule.clickOn(res = CommonStrings.action_retry)
        eventsRecorder.assertSingle(ResolveVerifiedUserSendFailureEvents.Retry)
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setResolveVerifiedUserSendFailureView(
        state: ResolveVerifiedUserSendFailureState,
    ) {
        setSafeContent {
            ResolveVerifiedUserSendFailureView(state = state)
        }
    }
}
