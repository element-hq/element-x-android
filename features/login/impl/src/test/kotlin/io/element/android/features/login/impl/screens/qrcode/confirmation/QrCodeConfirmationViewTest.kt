/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.confirmation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBackKey
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrCodeConfirmationViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `on back pressed - calls the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setQrCodeConfirmationView(
                step = QrCodeConfirmationStep.DisplayCheckCode("12"),
                onCancel = callback
            )
            rule.pressBackKey()
        }
    }

    @Test
    fun `on Cancel button clicked - calls the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setQrCodeConfirmationView(
                step = QrCodeConfirmationStep.DisplayVerificationCode("123456"),
                onCancel = callback
            )
            rule.clickOn(CommonStrings.action_cancel)
        }
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setQrCodeConfirmationView(
        step: QrCodeConfirmationStep,
        onCancel: () -> Unit
    ) {
        setContent {
            QrCodeConfirmationView(
                step = step,
                onCancel = onCancel
            )
        }
    }
}
