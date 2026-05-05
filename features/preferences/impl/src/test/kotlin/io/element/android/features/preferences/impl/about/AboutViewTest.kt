/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.preferences.impl.about

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutViewTest {
    @Test
    fun `clicking on back invokes back callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setAboutView(
                anAboutState(),
                onBackClick = callback,
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on an item invokes the expected callback`() = runAndroidComposeUiTest {
        val state = anAboutState()
        ensureCalledOnceWithParam(state.elementLegals.first()) { callback ->
            setAboutView(
                state,
                onElementLegalClick = callback,
            )
            clickOn(state.elementLegals.first().titleRes)
        }
    }

    @Test
    fun `clicking on the open source licenses invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setAboutView(
                anAboutState(),
                onOpenSourceLicensesClick = callback,
            )
            clickOn(CommonStrings.common_open_source_licenses)
        }
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setAboutView(
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
