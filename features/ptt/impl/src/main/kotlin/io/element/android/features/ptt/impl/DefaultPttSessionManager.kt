/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.ptt.api.PttChannelConfig
import io.element.android.features.ptt.api.PttFeatureAvailability
import io.element.android.features.ptt.api.PttSessionManager
import io.element.android.features.ptt.api.PttSessionState
import io.element.android.features.ptt.api.PttTransportType
import io.element.android.features.ptt.impl.services.PttSessionHostService
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Facade over the foreground [PttSessionHostService] (session lifecycle) and the app-scoped
 * [PttSessionController] (floor/state), so any feature can drive a PTT session through the
 * [PttSessionManager] api without depending on the transport internals.
 */
@ContributesBinding(AppScope::class)
class DefaultPttSessionManager(
    @ApplicationContext private val context: Context,
    private val controller: PttSessionController,
    private val settings: PttLocalSettings,
    private val availability: PttFeatureAvailability,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
) : PttSessionManager {
    init {
        controller.setCovert(settings.covert)
    }

    override val sessionState: StateFlow<PttSessionState?> = controller.sessionState

    override val isCovert: StateFlow<Boolean> = controller.isCovert

    override fun start(sessionId: SessionId, roomId: RoomId) {
        coroutineScope.launch {
            if (availability.isAvailable.first()) {
                PttSessionHostService.start(context, interimMumbleConfig(sessionId, roomId, settings.server(sessionId)))
            }
        }
    }

    override fun stop() {
        PttSessionHostService.hangup(context)
    }

    override fun pressToTalk() = controller.pressToTalk()

    override fun releaseToTalk() = controller.releaseToTalk()

    override fun setCovert(enabled: Boolean) {
        settings.setCovert(enabled)
        controller.setCovert(enabled)
    }
}

/**
 * INTERIM channel config until task #8 resolves it from the room's `io.element.ptt.config` state
 * event. Point [MURMUR_HOST] at your Murmur: `10.0.2.2` is the emulator's host-loopback alias; use
 * the host machine's LAN IP for physical devices.
 */
internal const val MURMUR_HOST = "192.168.4.26"

internal fun interimMumbleConfig(sessionId: SessionId, roomId: RoomId, server: PttServerSettings = PttServerSettings()) = PttChannelConfig(
    sessionId = sessionId,
    roomId = roomId,
    transport = PttTransportType.Mumble,
    transportMetadata = mapOf(
        "host" to server.host.trim().ifEmpty { MURMUR_HOST },
        "port" to server.port.trim(),
        "password" to server.password,
        "username" to server.username.trim().ifEmpty { sessionId.value.substringAfter("@").substringBefore(":") },
    ),
)
