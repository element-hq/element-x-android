/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.roomdetails.impl

import androidx.activity.ComponentActivity
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class NoopRoomDetailsExtensionTest : RobolectricTest() {
    @Test
    fun `default extension renders no additional content`() = runAndroidComposeUiTest<ComponentActivity> {
        val extension = NoopRoomDetailsExtension()
        setContent { extension.Render(Modifier) }
        onRoot().onChildren().assertCountEquals(0)
    }

    @Test
    fun `default extension leaves surrounding content unchanged during updates`() = runAndroidComposeUiTest<ComponentActivity> {
        val extension = NoopRoomDetailsExtension()
        val text = mutableStateOf("Before")
        setContent {
            BasicText(text.value)
            extension.Render(Modifier)
        }
        onNodeWithText("Before").assertExists()
        onRoot().onChildren().assertCountEquals(1)
        runOnIdle { text.value = "After" }
        onNodeWithText("After").assertExists()
        onRoot().onChildren().assertCountEquals(1)
    }
}
