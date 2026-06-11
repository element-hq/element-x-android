/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.linknewdevice.impl.screens.desktop

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.linknewdevice.impl.R
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.pressBackKey
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DesktopNoticeViewTest {
    @Test
    fun `on back pressed - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                state = aDesktopNoticeState(),
                onBackClicked = callback,
            )
            pressBackKey()
        }
    }

    @Test
    fun `on back button clicked - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                state = aDesktopNoticeState(),
                onBackClicked = callback,
            )
            pressBack()
        }
    }

    @Test
    fun `when can continue - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                state = aDesktopNoticeState(canContinue = true),
                onReadyToScanClick = callback,
            )
        }
    }

    @Test
    fun `on submit button clicked - emits the Continue event`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<DesktopNoticeEvent>()
        setView(
            state = aDesktopNoticeState(eventSink = eventRecorder),
        )
        clickOn(R.string.screen_link_new_device_desktop_submit)
        eventRecorder.assertSingle(DesktopNoticeEvent.Continue)
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setView(
        state: DesktopNoticeState,
        onBackClicked: () -> Unit = EnsureNeverCalled(),
        onReadyToScanClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            DesktopNoticeView(
                state = state,
                onBackClick = onBackClicked,
                onReadyToScanClick = onReadyToScanClick,
            )
        }
    }
}
