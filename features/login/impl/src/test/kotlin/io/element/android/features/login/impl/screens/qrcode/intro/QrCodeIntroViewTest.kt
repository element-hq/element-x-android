/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.intro

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.login.impl.R
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
class QrCodeIntroViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `on back pressed - calls the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setQrCodeIntroView(
                state = aQrCodeIntroState(),
                onBackClicked = callback
            )
            rule.pressBackKey()
        }
    }

    @Test
    fun `on back button clicked - calls the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setQrCodeIntroView(
                state = aQrCodeIntroState(),
                onBackClicked = callback
            )
            rule.pressBack()
        }
    }

    @Test
    fun `when can continue - calls the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setQrCodeIntroView(
                state = aQrCodeIntroState(canContinue = true),
                onContinue = callback
            )
        }
    }

    @Test
    fun `on submit button clicked - emits the Continue event`() {
        val eventRecorder = EventsRecorder<QrCodeIntroEvents>()
        rule.setQrCodeIntroView(
            state = aQrCodeIntroState(eventSink = eventRecorder),
        )
        rule.clickOn(R.string.screen_qr_code_login_initial_state_button_title)
        eventRecorder.assertSingle(QrCodeIntroEvents.Continue)
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setQrCodeIntroView(
        state: QrCodeIntroState,
        onBackClicked: () -> Unit = EnsureNeverCalled(),
        onContinue: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            QrCodeIntroView(
                state = state,
                onBackClick = onBackClicked,
                onContinue = onContinue,
            )
        }
    }
}
