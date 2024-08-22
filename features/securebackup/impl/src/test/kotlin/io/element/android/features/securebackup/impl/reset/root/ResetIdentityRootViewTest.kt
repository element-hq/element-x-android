/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.securebackup.impl.reset.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.ui.strings.CommonStrings
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
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class ResetIdentityRootViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `pressing the back HW button invokes the expected callback`() {
        ensureCalledOnce {
            rule.setResetRootView(
                onBack = it,
            )
            rule.pressBackKey()
        }
    }

    @Test
    fun `clicking on the back navigation button invokes the expected callback`() {
        ensureCalledOnce {
            rule.setResetRootView(
                onBack = it,
            )
            rule.pressBack()
        }
    }

    @Test
    @Config(qualifiers = "h720dp")
    fun `clicking 'Continue' invokes the expected callback`() {
        ensureCalledOnce {
            rule.setResetRootView(
                onContinue = it,
            )
            rule.clickOn(CommonStrings.action_continue)
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setResetRootView(
    onBack: () -> Unit = EnsureNeverCalled(),
    onContinue: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        ResetIdentityRootView(onContinue = onContinue, onBack = onBack)
    }
}
