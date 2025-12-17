/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.scan

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.test.auth.qrlogin.FakeMatrixQrCodeLoginData
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBackKey
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrCodeScanViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `on back pressed - calls the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setQrCodeScanView(
                state = aQrCodeScanState(),
                onBackClick = callback
            )
            rule.pressBackKey()
        }
    }

    @Test
    fun `on QR code data ready - calls the expected callback`() {
        val data = FakeMatrixQrCodeLoginData()
        ensureCalledOnceWithParam<MatrixQrCodeLoginData>(data) { callback ->
            rule.setQrCodeScanView(
                state = aQrCodeScanState(authenticationAction = AsyncAction.Success(data)),
                onQrCodeDataReady = callback
            )
        }
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setQrCodeScanView(
        state: QrCodeScanState,
        onBackClick: () -> Unit = EnsureNeverCalled(),
        onQrCodeDataReady: (MatrixQrCodeLoginData) -> Unit = EnsureNeverCalledWithParam(),
    ) {
        setContent {
            QrCodeScanView(
                state = state,
                onBackClick = onBackClick,
                onQrCodeDataReady = onQrCodeDataReady
            )
        }
    }
}
