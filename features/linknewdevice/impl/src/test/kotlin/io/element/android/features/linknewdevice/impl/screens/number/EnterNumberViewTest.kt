/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.linknewdevice.impl.screens.number

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.pressBackKey
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnterNumberViewTest {
    @Test
    fun `on back pressed - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                state = aEnterNumberState(),
                onBackClicked = callback,
            )
            pressBackKey()
        }
    }

    @Test
    fun `on back button clicked - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                state = aEnterNumberState(),
                onBackClicked = callback,
            )
            pressBack()
        }
    }

    @Test
    fun `on continue button clicked - emits the Continue event`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<EnterNumberEvent>()
        setView(
            state = aEnterNumberState(
                number = "12",
                eventSink = eventRecorder,
            ),
        )
        clickOn(CommonStrings.action_continue)
        eventRecorder.assertSingle(EnterNumberEvent.Continue)
    }

    @Test
    fun `when the number is not complete, continue button is disabled`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<EnterNumberEvent>(expectEvents = false)
        setView(
            state = aEnterNumberState(
                number = "1",
                eventSink = eventRecorder,
            ),
        )
        val continueStr = activity!!.getString(CommonStrings.action_continue)
        onNodeWithText(continueStr).assertIsNotEnabled()
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setView(
        state: EnterNumberState,
        onBackClicked: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            EnterNumberView(
                state = state,
                onBackClick = onBackClicked,
            )
        }
    }
}
