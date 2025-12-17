/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.about

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
    fun `clicking on the open source licenses invokes the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setAboutView(
                anAboutState(),
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
