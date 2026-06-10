/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.linknewdevice.impl.screens.error

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBackKey
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorViewTest {
    @Test
    fun `on back pressed - calls the onCancel callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setErrorView(
                onCancel = callback,
            )
            pressBackKey()
        }
    }

    @Test
    fun `on try again button clicked - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setErrorView(
                onRetry = callback
            )
            clickOn(CommonStrings.action_try_again)
        }
    }

    @Test
    fun `on cancel button clicked - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setErrorView(
                onCancel = callback
            )
            clickOn(CommonStrings.action_cancel)
        }
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setErrorView(
        onRetry: () -> Unit = EnsureNeverCalled(),
        onCancel: () -> Unit = EnsureNeverCalled(),
        errorScreenType: ErrorScreenType = ErrorScreenType.UnknownError,
    ) {
        setContent {
            ErrorView(
                errorScreenType = errorScreenType,
                onRetry = onRetry,
                onCancel = onCancel,
            )
        }
    }
}
