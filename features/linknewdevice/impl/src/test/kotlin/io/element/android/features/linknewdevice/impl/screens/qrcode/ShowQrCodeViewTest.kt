/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.linknewdevice.impl.screens.qrcode

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBackKey
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShowQrCodeViewTest {
    @Test
    fun `on back pressed - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                onBackClick = callback
            )
            pressBackKey()
        }
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setView(
        onBackClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            ShowQrCodeView(
                data = "DATA",
                onBackClick = onBackClick,
            )
        }
    }
}
