/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.login.impl.screens.qrcode.error

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.login.impl.qrcode.QrCodeErrorScreenType
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBackKey
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrCodeErrorViewTest {
    @Test
    fun `on back pressed - calls the onCancel callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setQrCodeErrorView(
                onCancel = callback,
            )
            pressBackKey()
        }
    }

    @Test
    fun `on try again button clicked - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setQrCodeErrorView(
                onRetry = callback,
            )
            clickOn(CommonStrings.action_try_again)
        }
    }

    @Test
    fun `on cancel button clicked - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setQrCodeErrorView(
                onCancel = callback,
            )
            clickOn(CommonStrings.action_cancel)
        }
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setQrCodeErrorView(
        onRetry: () -> Unit = EnsureNeverCalled(),
        onCancel: () -> Unit = EnsureNeverCalled(),
        errorScreenType: QrCodeErrorScreenType = QrCodeErrorScreenType.UnknownError,
        appName: String = "Element X",
    ) {
        setContent {
            QrCodeErrorView(
                errorScreenType = errorScreenType,
                appName = appName,
                onRetry = onRetry,
                onCancel = onCancel,
            )
        }
    }
}
