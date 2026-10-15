/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.ptt.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.google.common.truth.Truth.assertThat
import io.element.android.features.ptt.api.PttBluetoothState
import io.element.android.features.ptt.api.PttBluetoothStatus
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class PttBluetoothSettingsViewTest : RobolectricTest() {
    @Test
    fun `find button opens setup without changing room mode`() = runAndroidComposeUiTest<ComponentActivity> {
        val events = mutableListOf<PttBluetoothSettingsEvent>()
        setContent {
            PttBluetoothSettingsView(
                state = PttBluetoothSettingsState(
                    eventSink = events::add,
                    device = PttBluetoothState(
                        deviceName = null,
                        status = PttBluetoothStatus.Disconnected,
                        autoConnect = true,
                        isPressed = false,
                        eventCount = 0,
                    ),
                ),
                onBackClick = {},
            )
        }
        onNodeWithText("Find PTT button").performClick()
        assertThat(events).containsExactly(PttBluetoothSettingsEvent.Find)
    }

    @Test
    fun `ready screen shows non transmitting test and disconnect control`() = runAndroidComposeUiTest<ComponentActivity> {
        val events = mutableListOf<PttBluetoothSettingsEvent>()
        val state = PttBluetoothSettingsState(
            PttBluetoothState(
                deviceName = "PTT-Z",
                status = PttBluetoothStatus.Ready,
                isPressed = true,
                eventCount = 3,
                autoConnect = true,
            ),
            events::add,
        )
        setContent { PttBluetoothSettingsView(state, onBackClick = {}) }
        onNodeWithText("Button pressed").performScrollTo().assertExists()
        onNodeWithText("Disconnect").performScrollTo().performClick()
        assertThat(events).containsExactly(PttBluetoothSettingsEvent.Disconnect)
    }

    @Test
    fun `forget requires confirmation and explains system pairing remains`() = runAndroidComposeUiTest<ComponentActivity> {
        val events = mutableListOf<PttBluetoothSettingsEvent>()
        val state = PttBluetoothSettingsState(
            device = PttBluetoothState(
                deviceName = "PTT-Z",
                status = PttBluetoothStatus.Disconnected,
                autoConnect = true,
                isPressed = false,
                eventCount = 0,
            ),
            eventSink = events::add,
        )
        setContent { PttBluetoothSettingsView(state, onBackClick = {}) }
        onNodeWithText("Forget button in this app").performScrollTo().performClick()
        assertThat(events).isEmpty()
        onNodeWithText("Disconnect and remove this app’s saved button. The phone’s Bluetooth pairing will remain.").assertExists()
        onAllNodesWithText("Forget button in this app").onLast().performClick()
        assertThat(events).containsExactly(PttBluetoothSettingsEvent.Forget)
    }
}
