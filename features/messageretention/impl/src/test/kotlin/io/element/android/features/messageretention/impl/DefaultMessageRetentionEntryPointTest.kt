/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package io.element.android.features.messageretention.impl

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messageretention.api.MessageRetentionEntryPoint
import io.element.android.tests.testutils.robolectric.RobolectricTest
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultMessageRetentionEntryPointTest : RobolectricTest() {
    @Test
    fun `retention is unavailable in the default build`() = runTest {
        assertThat(DefaultMessageRetentionEntryPoint().isAvailable().first()).isFalse()
    }

    @Test
    fun `default node adds no content and does not navigate during updates`() = runAndroidComposeUiTest<ComponentActivity> {
        val callback = object : MessageRetentionEntryPoint.Callback {
            override fun onDone() = error("Unexpected navigation")
        }
        val node = DefaultMessageRetentionEntryPoint().createNode(mockk(), BuildContext.root(null), callback)
        assertThat(node.plugins).contains(callback)
        val text = mutableStateOf("Before")
        setContent {
            Column {
                BasicText(text.value)
                node.View(Modifier)
            }
        }
        onRoot().onChildren().assertCountEquals(1)
        runOnIdle { text.value = "After" }
        onNodeWithText("After").assertExists()
        onRoot().onChildren().assertCountEquals(1)
    }
}
