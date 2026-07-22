/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.utils

import dev.zacsweers.metro.Inject
import io.element.android.features.call.impl.data.DeviceMuteData
import io.element.android.features.call.impl.data.WidgetMessage
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Remembers microphone/camera enabled state across group calls without fighting Element Call's lobby.
 * Direct (1:1) calls do not restore or persist these preferences.
 *
 * Strategy:
 * - Camera is applied up-front via CallIntent/URL (see [DefaultCallWidgetProvider]).
 * - Microphone is only corrected via `device_mute` when it should start muted, and only after
 *   Element Call reports that media devices are ready (avoids false/false startup spam).
 * - Preferences are written only when the call ends, and only if media was ready at least once.
 */
@Inject
class CallDeviceMuteController(
    private val appPreferencesStore: AppPreferencesStore,
    private val jsonProvider: JsonProvider,
    private val widgetMessageSerializer: WidgetMessageSerializer,
    private val clock: SystemClock,
) {
    private val rememberLastMediaState = AtomicBoolean(false)
    private val mediaReady = AtomicBoolean(false)
    private val microphoneMuteApplied = AtomicBoolean(false)
    private val preferMicrophoneEnabled = AtomicReference(true)
    private val lastMicrophoneEnabled = AtomicReference<Boolean?>(null)
    private val lastCameraEnabled = AtomicReference<Boolean?>(null)

    fun setRememberLastMediaState(enabled: Boolean) {
        rememberLastMediaState.set(enabled)
    }

    suspend fun onContentLoaded() {
        if (!rememberLastMediaState.get()) return
        val microphoneEnabled = appPreferencesStore.isCallMicrophoneEnabledFlow().first()
        val cameraEnabled = appPreferencesStore.isCallCameraEnabledFlow().first()
        preferMicrophoneEnabled.set(microphoneEnabled)
        lastMicrophoneEnabled.set(microphoneEnabled)
        lastCameraEnabled.set(cameraEnabled)
        Timber.d("Call media prefs loaded: microphoneEnabled=$microphoneEnabled, cameraEnabled=$cameraEnabled")
    }

    fun onDeviceMuteFromWidget(
        message: WidgetMessage,
        widgetId: String,
        messageInterceptor: WidgetMessageInterceptor,
    ) {
        if (!rememberLastMediaState.get()) return
        val muteData = parseDeviceMuteData(message) ?: return
        val audioEnabled = muteData.audioEnabled
        val videoEnabled = muteData.videoEnabled

        if (!mediaReady.get()) {
            // Element Call emits audio=false,video=false while capture devices are unavailable.
            // Treat the first report with any device enabled as "media ready".
            if (audioEnabled == true || videoEnabled == true) {
                mediaReady.set(true)
                Timber.d("Element Call media devices are ready")
            } else {
                return
            }
        }

        audioEnabled?.let { lastMicrophoneEnabled.set(it) }
        videoEnabled?.let { lastCameraEnabled.set(it) }

        maybeMuteMicrophone(widgetId, messageInterceptor)
    }

    suspend fun persistIfReady() {
        if (!rememberLastMediaState.get()) return
        if (!mediaReady.get()) {
            Timber.d("Skip persisting call media prefs: media was never ready")
            return
        }
        val microphoneEnabled = lastMicrophoneEnabled.get()
        val cameraEnabled = lastCameraEnabled.get()
        microphoneEnabled?.let { appPreferencesStore.setCallMicrophoneEnabled(it) }
        cameraEnabled?.let { appPreferencesStore.setCallCameraEnabled(it) }
    }

    private fun maybeMuteMicrophone(widgetId: String, messageInterceptor: WidgetMessageInterceptor) {
        if (preferMicrophoneEnabled.get()) return
        if (!mediaReady.get()) return
        if (!microphoneMuteApplied.compareAndSet(false, true)) return

        Timber.d("Muting microphone to match saved preference")
        val message = WidgetMessage(
            direction = WidgetMessage.Direction.ToWidget,
            widgetId = widgetId,
            requestId = "widgetapi-${clock.epochMillis()}",
            action = WidgetMessage.Action.DeviceMute,
            // Only touch the microphone. Camera is controlled by CallIntent.
            data = jsonProvider().encodeToJsonElement(DeviceMuteData(audioEnabled = false)),
        )
        messageInterceptor.sendMessage(widgetMessageSerializer.serialize(message))
        lastMicrophoneEnabled.set(false)
    }

    private fun parseDeviceMuteData(message: WidgetMessage): DeviceMuteData? {
        val data = message.data ?: return null
        return runCatchingExceptions {
            jsonProvider().decodeFromJsonElement<DeviceMuteData>(data)
        }.getOrElse {
            Timber.w(it, "Failed to parse device mute payload")
            null
        }
    }
}
