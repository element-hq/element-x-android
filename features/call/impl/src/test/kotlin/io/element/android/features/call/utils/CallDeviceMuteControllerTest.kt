/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.utils

import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.impl.data.DeviceMuteData
import io.element.android.features.call.impl.data.WidgetMessage
import io.element.android.features.call.impl.utils.CallDeviceMuteController
import io.element.android.features.call.impl.utils.WidgetMessageSerializer
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.Test

class CallDeviceMuteControllerTest {
    @Test
    fun `ignores pre-ready false false device mute updates`() = runTest {
        val preferences = InMemoryAppPreferencesStore(
            callMicrophoneEnabled = true,
            callCameraEnabled = true,
        )
        val interceptor = FakeWidgetMessageInterceptor()
        val controller = createController(preferences)

        controller.setRememberLastMediaState(true)
        controller.onContentLoaded()
        controller.onDeviceMuteFromWidget(
            message = deviceMuteMessage(audioEnabled = false, videoEnabled = false),
            widgetId = "widget-1",
            messageInterceptor = interceptor,
        )
        controller.persistIfReady()

        assertThat(interceptor.sentMessages).isEmpty()
        assertThat(preferences.isCallMicrophoneEnabledFlow().first()).isTrue()
        assertThat(preferences.isCallCameraEnabledFlow().first()).isTrue()
    }

    @Test
    fun `tracks updates after media ready and persists on end`() = runTest {
        val preferences = InMemoryAppPreferencesStore(
            callMicrophoneEnabled = true,
            callCameraEnabled = true,
        )
        val interceptor = FakeWidgetMessageInterceptor()
        val controller = createController(preferences)

        controller.setRememberLastMediaState(true)
        controller.onContentLoaded()
        controller.onDeviceMuteFromWidget(
            message = deviceMuteMessage(audioEnabled = true, videoEnabled = true),
            widgetId = "widget-1",
            messageInterceptor = interceptor,
        )
        controller.onDeviceMuteFromWidget(
            message = deviceMuteMessage(audioEnabled = false, videoEnabled = true),
            widgetId = "widget-1",
            messageInterceptor = interceptor,
        )
        controller.persistIfReady()

        assertThat(interceptor.sentMessages).isEmpty()
        assertThat(preferences.isCallMicrophoneEnabledFlow().first()).isFalse()
        assertThat(preferences.isCallCameraEnabledFlow().first()).isTrue()
    }

    @Test
    fun `mutes microphone once after media ready when preference is disabled`() = runTest {
        val preferences = InMemoryAppPreferencesStore(
            callMicrophoneEnabled = false,
            callCameraEnabled = true,
        )
        val interceptor = FakeWidgetMessageInterceptor()
        val controller = createController(preferences)

        controller.setRememberLastMediaState(true)
        controller.onContentLoaded()
        controller.onDeviceMuteFromWidget(
            message = deviceMuteMessage(audioEnabled = true, videoEnabled = true),
            widgetId = "widget-1",
            messageInterceptor = interceptor,
        )
        // Second ready update must not send another mute request.
        controller.onDeviceMuteFromWidget(
            message = deviceMuteMessage(audioEnabled = true, videoEnabled = true),
            widgetId = "widget-1",
            messageInterceptor = interceptor,
        )

        assertThat(interceptor.sentMessages).hasSize(1)
        val sent = interceptor.sentMessages.single()
        assertThat(sent).contains("\"action\":\"device_mute\"")
        assertThat(sent).contains("\"audio_enabled\":false")
        assertThat(sent).doesNotContain("\"video_enabled\"")
    }

    @Test
    fun `does not persist when media never became ready`() = runTest {
        val preferences = InMemoryAppPreferencesStore(
            callMicrophoneEnabled = true,
            callCameraEnabled = false,
        )
        val controller = createController(preferences)

        controller.setRememberLastMediaState(true)
        controller.onContentLoaded()
        controller.persistIfReady()

        assertThat(preferences.isCallMicrophoneEnabledFlow().first()).isTrue()
        assertThat(preferences.isCallCameraEnabledFlow().first()).isFalse()
    }

    @Test
    fun `does nothing when rememberLastMediaState is disabled`() = runTest {
        val preferences = InMemoryAppPreferencesStore(
            callMicrophoneEnabled = false,
            callCameraEnabled = false,
        )
        val interceptor = FakeWidgetMessageInterceptor()
        val controller = createController(preferences)

        controller.setRememberLastMediaState(false)
        controller.onContentLoaded()
        controller.onDeviceMuteFromWidget(
            message = deviceMuteMessage(audioEnabled = true, videoEnabled = true),
            widgetId = "widget-1",
            messageInterceptor = interceptor,
        )
        controller.onDeviceMuteFromWidget(
            message = deviceMuteMessage(audioEnabled = false, videoEnabled = false),
            widgetId = "widget-1",
            messageInterceptor = interceptor,
        )
        controller.persistIfReady()

        assertThat(interceptor.sentMessages).isEmpty()
        assertThat(preferences.isCallMicrophoneEnabledFlow().first()).isFalse()
        assertThat(preferences.isCallCameraEnabledFlow().first()).isFalse()
    }

    private fun createController(
        preferences: InMemoryAppPreferencesStore,
    ): CallDeviceMuteController {
        val jsonProvider = DefaultJsonProvider()
        return CallDeviceMuteController(
            appPreferencesStore = preferences,
            jsonProvider = jsonProvider,
            widgetMessageSerializer = WidgetMessageSerializer(jsonProvider),
            clock = { 0 },
        )
    }

    private fun deviceMuteMessage(
        audioEnabled: Boolean?,
        videoEnabled: Boolean?,
    ): WidgetMessage {
        val json = DefaultJsonProvider()
        return WidgetMessage(
            direction = WidgetMessage.Direction.FromWidget,
            widgetId = "widget-1",
            requestId = "1",
            action = WidgetMessage.Action.DeviceMute,
            data = json().encodeToJsonElement(
                DeviceMuteData(
                    audioEnabled = audioEnabled,
                    videoEnabled = videoEnabled,
                )
            ),
        )
    }
}
