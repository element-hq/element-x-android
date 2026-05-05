/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.messages.impl.timeline.protection

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.lambda.lambdaError
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProtectedViewTest {
    @Test
    fun `when hideContent is false, the content is rendered`() = runAndroidComposeUiTest {
        setProtectedView(
            hideContent = false,
            content = {
                Text("Hello")
            }
        )
        onNodeWithText("Hello").assertExists()
    }

    @Test
    fun `when hideContent is true, the content is not rendered, and user can reveal it`() = runAndroidComposeUiTest {
        ensureCalledOnce {
            setProtectedView(
                hideContent = true,
                onShowClick = it,
                content = {
                    Text("Hello")
                }
            )
            onNodeWithText("Hello").assertDoesNotExist()
            clickOn(CommonStrings.action_show)
        }
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setProtectedView(
    hideContent: Boolean = false,
    onShowClick: () -> Unit = { lambdaError() },
    content: @Composable () -> Unit = {},
) {
    setContent {
        ProtectedView(
            hideContent = hideContent,
            onShowClick = onShowClick,
            content = content
        )
    }
}
