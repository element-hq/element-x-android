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

package io.element.android.features.preferences.impl.about

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes back callback`() {
        ensureCalledOnce { callback ->
            rule.setAboutView(
                anAboutState(),
                onBackClick = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on an item invokes the expected callback`() {
        val state = anAboutState()
        ensureCalledOnceWithParam(state.elementLegals.first()) { callback ->
            rule.setAboutView(
                state,
                onElementLegalClick = callback,
            )
            rule.clickOn(state.elementLegals.first().titleRes)
        }
    }

    @Test
    fun `if open source licenses are not available, the entry is not displayed`() {
        rule.setAboutView(
            anAboutState(),
        )
        val text = rule.activity.getString(CommonStrings.common_open_source_licenses)
        rule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun `if open source licenses are available, clicking on the entry invokes the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setAboutView(
                anAboutState(
                    hasOpenSourcesLicenses = true,
                ),
                onOpenSourceLicensesClick = callback,
            )
            rule.clickOn(CommonStrings.common_open_source_licenses)
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setAboutView(
    state: AboutState,
    onElementLegalClick: (ElementLegal) -> Unit = EnsureNeverCalledWithParam(),
    onOpenSourceLicensesClick: () -> Unit = EnsureNeverCalled(),
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        AboutView(
            state = state,
            onElementLegalClick = onElementLegalClick,
            onOpenSourceLicensesClick = onOpenSourceLicensesClick,
            onBackClick = onBackClick,
        )
    }
}
