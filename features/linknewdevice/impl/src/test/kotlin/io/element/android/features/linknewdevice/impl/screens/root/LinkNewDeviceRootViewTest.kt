/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.linknewdevice.impl.screens.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.linknewdevice.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBackKey
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LinkNewDeviceRootViewTest {
    @Test
    fun `on back pressed - calls the onRetry callback`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<LinkNewDeviceRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setLinkNewDeviceRootView(
                state = aLinkNewDeviceRootState(
                    eventSink = eventRecorder,
                ),
                onBackClick = callback
            )
            pressBackKey()
        }
    }

    @Test
    fun `link desktop button clicked - calls the expected callback`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<LinkNewDeviceRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setLinkNewDeviceRootView(
                state = aLinkNewDeviceRootState(
                    isSupported = AsyncData.Success(true),
                    eventSink = eventRecorder,
                ),
                onLinkDesktopDeviceClick = callback,
            )
            clickOn(R.string.screen_link_new_device_root_desktop_computer)
        }
    }

    @Test
    fun `link mobile button clicked - emits the expected event`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<LinkNewDeviceRootEvent>()
        setLinkNewDeviceRootView(
            state = aLinkNewDeviceRootState(
                isSupported = AsyncData.Success(true),
                eventSink = eventRecorder,
            )
        )
        clickOn(R.string.screen_link_new_device_root_mobile_device)
        eventRecorder.assertSingle(LinkNewDeviceRootEvent.LinkMobileDevice)
    }

    @Test
    fun `not supported - dismiss click - invokes the expected callback`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<LinkNewDeviceRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setLinkNewDeviceRootView(
                state = aLinkNewDeviceRootState(
                    isSupported = AsyncData.Success(false),
                    eventSink = eventRecorder,
                ),
                onBackClick = callback,
            )
            clickOn(CommonStrings.action_dismiss)
        }
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setLinkNewDeviceRootView(
        state: LinkNewDeviceRootState = aLinkNewDeviceRootState(),
        onBackClick: () -> Unit = EnsureNeverCalled(),
        onLinkDesktopDeviceClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            LinkNewDeviceRootView(
                state = state,
                onBackClick = onBackClick,
                onLinkDesktopDeviceClick = onLinkDesktopDeviceClick,
            )
        }
    }
}
