/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.libraries.textcomposer

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.element.android.libraries.textcomposer.components.PushToTalkButton
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PushToTalkButtonTest : RobolectricTest() {
    @Test
    fun `accessibility activation starts and stops without holding`() = runAndroidComposeUiTest<ComponentActivity> {
        val events = mutableListOf<String>()
        setContent { PushToTalkButton({ events.add("start") }, { events.add("stop") }) }
        val button = onNodeWithContentDescription(activity!!.getString(CommonStrings.a11y_ptt_button))
        button.performSemanticsAction(SemanticsActions.OnClick) { it() }
        runOnIdle { assertEquals(listOf("start"), events) }
        button.performSemanticsAction(SemanticsActions.OnClick) { it() }
        runOnIdle { assertEquals(listOf("start", "stop"), events) }
    }

    @Test
    fun `touch release stops transmission`() = runAndroidComposeUiTest<ComponentActivity> {
        val events = mutableListOf<String>()
        setContent { PushToTalkButton({ events.add("start") }, { events.add("stop") }) }
        val button = onNodeWithContentDescription(activity!!.getString(CommonStrings.a11y_ptt_button))
        button.performTouchInput { down(center) }
        runOnIdle { assertEquals(listOf("start"), events) }
        button.performTouchInput { up() }
        runOnIdle { assertEquals(listOf("start", "stop"), events) }
    }

    @Test
    fun `touch cancellation stops transmission`() = runAndroidComposeUiTest<ComponentActivity> {
        val events = mutableListOf<String>()
        setContent { PushToTalkButton({ events.add("start") }, { events.add("stop") }) }
        val button = onNodeWithContentDescription(activity!!.getString(CommonStrings.a11y_ptt_button))
        button.performTouchInput { down(center) }
        button.performTouchInput { cancel() }
        runOnIdle { assertEquals(listOf("start", "stop"), events) }
    }

    @Test
    fun `removing the button stops a latched request`() = runAndroidComposeUiTest<ComponentActivity> {
        val visible = mutableStateOf(true)
        val events = mutableListOf<String>()
        setContent { if (visible.value) PushToTalkButton({ events.add("start") }, { events.add("stop") }) }
        onNodeWithContentDescription(activity!!.getString(CommonStrings.a11y_ptt_button))
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        runOnIdle { visible.value = false }
        runOnIdle { assertEquals(listOf("start", "stop"), events) }
    }

    @Test
    fun `keyboard activation toggles and escape stops`() = runAndroidComposeUiTest<ComponentActivity> {
        val events = mutableListOf<String>()
        setContent { PushToTalkButton({ events.add("start") }, { events.add("stop") }) }
        val button = onNodeWithContentDescription(activity!!.getString(CommonStrings.a11y_ptt_button))
        button.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        button.performKeyInput { pressKey(Key.Enter) }
        button.performKeyInput { pressKey(Key.Enter) }
        button.performKeyInput { pressKey(Key.Spacebar) }
        button.performKeyInput { pressKey(Key.Escape) }
        runOnIdle { assertEquals(listOf("start", "stop", "start", "stop"), events) }
    }

    @Test
    fun `pausing stops a latched request`() = runAndroidComposeUiTest<ComponentActivity> {
        val owner = object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry.createUnsafe(this)
        }
        owner.lifecycle.currentState = Lifecycle.State.RESUMED
        val events = mutableListOf<String>()
        setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                PushToTalkButton({ events.add("start") }, { events.add("stop") })
            }
        }
        onNodeWithContentDescription(activity!!.getString(CommonStrings.a11y_ptt_button))
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        runOnIdle { owner.lifecycle.currentState = Lifecycle.State.STARTED }
        runOnIdle { assertEquals(listOf("start", "stop"), events) }
    }
}
