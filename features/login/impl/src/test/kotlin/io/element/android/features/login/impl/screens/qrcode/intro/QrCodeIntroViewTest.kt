/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.login.impl.screens.qrcode.intro

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.login.impl.R
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.pressBackKey
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrCodeIntroViewTest {
    @Test
    fun `on back pressed - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setQrCodeIntroView(
                state = aQrCodeIntroState(),
                onBackClicked = callback
            )
            pressBackKey()
        }
    }

    @Test
    fun `on back button clicked - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setQrCodeIntroView(
                state = aQrCodeIntroState(),
                onBackClicked = callback
            )
            pressBack()
        }
    }

    @Test
    fun `when can continue - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setQrCodeIntroView(
                state = aQrCodeIntroState(canContinue = true),
                onContinue = callback
            )
        }
    }

    @Test
    fun `on submit button clicked - emits the Continue event`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<QrCodeIntroEvents>()
        setQrCodeIntroView(
            state = aQrCodeIntroState(eventSink = eventRecorder),
        )
        clickOn(R.string.screen_qr_code_login_initial_state_button_title)
        eventRecorder.assertSingle(QrCodeIntroEvents.Continue)
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setQrCodeIntroView(
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
