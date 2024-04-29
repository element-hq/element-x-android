/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
                step = QrCodeConfirmationStep.DisplayCheckCode("12"),
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
