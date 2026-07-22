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
import dev.zacsweers.metro.Inject
import io.element.android.features.ptt.api.PttChannelConfig
import io.element.android.features.ptt.api.PttSessionManager
import io.element.android.features.ptt.api.PttSessionState
import io.element.android.features.ptt.api.PttTransportType
import io.element.android.features.ptt.impl.services.PttSessionHostService
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.StateFlow

/**
 * Facade over the foreground [PttSessionHostService] (session lifecycle) and the app-scoped
 * [PttSessionController] (floor/state), so any feature can drive a PTT session through the
 * [PttSessionManager] api without depending on the transport internals.
 */
@ContributesBinding(AppScope::class)
@Inject
class DefaultPttSessionManager(
    @ApplicationContext private val context: Context,
    private val controller: PttSessionController,
) : PttSessionManager {
    override val sessionState: StateFlow<PttSessionState?> = controller.sessionState

    override val isHearingEnabled: StateFlow<Boolean> = controller.isHearingEnabled

    override val isCovert: StateFlow<Boolean> = controller.isCovert

    override fun start(sessionId: SessionId, roomId: RoomId) {
        PttSessionHostService.start(context, interimMumbleConfig(sessionId, roomId))
    }

    override fun stop() {
        PttSessionHostService.hangup(context)
    }

    override fun pressToTalk() = controller.pressToTalk()

    override fun releaseToTalk() = controller.releaseToTalk()

    override fun setHearingEnabled(enabled: Boolean) = controller.setHearingEnabled(enabled)

    override fun setCovert(enabled: Boolean) = controller.setCovert(enabled)
}

/**
 * INTERIM channel config until task #8 resolves it from the room's `io.element.ptt.config` state
 * event. Point [MURMUR_HOST] at your Murmur: `10.0.2.2` is the emulator's host-loopback alias; use
 * the host machine's LAN IP for physical devices.
 */
private const val MURMUR_HOST = "10.0.2.2"

internal fun interimMumbleConfig(sessionId: SessionId, roomId: RoomId) = PttChannelConfig(
    sessionId = sessionId,
    roomId = roomId,
    transport = PttTransportType.Mumble,
    transportMetadata = mapOf(
        "host" to MURMUR_HOST,
        "port" to "64738",
        "username" to sessionId.value.substringAfter("@").substringBefore(":"),
    ),
)
