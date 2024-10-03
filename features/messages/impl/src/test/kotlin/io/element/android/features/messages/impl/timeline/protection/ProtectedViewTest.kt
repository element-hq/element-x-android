/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.lambda.lambdaError
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProtectedViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `when hideContent is false, the content is rendered`() {
        rule.setProtectedView(
            hideContent = false,
            content = {
                Text("Hello")
            }
        )
        rule.onNodeWithText("Hello").assertExists()
    }

    @Test
    fun `when hideContent is true, the content is not rendered, and user can reveal it`() {
        ensureCalledOnce {
            rule.setProtectedView(
                hideContent = true,
                onShowClick = it,
                content = {
                    Text("Hello")
                }
            )
            rule.onNodeWithText("Hello").assertDoesNotExist()
            rule.clickOn(CommonStrings.action_show)
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setProtectedView(
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
