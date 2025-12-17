/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.error

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.qrcode.QrCodeErrorScreenType
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBackKey
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrCodeErrorViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `on back pressed - calls the onRetry callback`() {
        ensureCalledOnce { callback ->
            rule.setQrCodeErrorView(
                onRetry = callback
            )
            rule.pressBackKey()
        }
    }

    @Test
    fun `on try again button clicked - calls the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setQrCodeErrorView(
                onRetry = callback
            )
            rule.clickOn(R.string.screen_qr_code_login_start_over_button)
        }
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setQrCodeErrorView(
        onRetry: () -> Unit,
        errorScreenType: QrCodeErrorScreenType = QrCodeErrorScreenType.UnknownError,
        appName: String = "Element X",
    ) {
        setContent {
            QrCodeErrorView(
                errorScreenType = errorScreenType,
                appName = appName,
                onRetry = onRetry
            )
        }
    }
}
