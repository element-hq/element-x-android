/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.ptt.api.PttChannelConfig
import io.element.android.features.ptt.api.PttSessionState
import io.element.android.features.ptt.api.PttTransmitResult
import io.element.android.features.ptt.api.PttTransport
import io.element.android.features.ptt.api.PttTransportFactory
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Session-host core: owns a single [PttTransport] lifecycle, created from the injected
 * [PttTransportFactory] for the channel's [PttChannelConfig.transport]. Transport-agnostic — the
 * foreground service / presenter drive it; it doesn't know Mumble from Element Call.
 *
 * Above this sits the transport-agnostic host concerns (foreground service, overlay, tones,
 * hardware buttons); below it, the selected [PttTransport] owns the audio session and floor.
 */
@SingleIn(RoomScope::class)
@Inject
class PttSessionController(
    private val transportFactory: PttTransportFactory,
    @SessionCoroutineScope private val coroutineScope: CoroutineScope,
) {
    private val mutex = Mutex()
    private var transport: PttTransport? = null
    private var stateJob: Job? = null

    private val _sessionState = MutableStateFlow<PttSessionState?>(null)

    /** The live session's state, or `null` when no channel is joined. */
    val sessionState: StateFlow<PttSessionState?> = _sessionState.asStateFlow()

    /** Create the transport for [config] and join its channel. Idempotent while a session is live. */
    suspend fun start(config: PttChannelConfig) {
        mutex.withLock {
            if (transport != null) return@withLock
            val newTransport = transportFactory.create(config)
            transport = newTransport
            stateJob = coroutineScope.launch {
                newTransport.state.collect { _sessionState.value = it }
            }
            newTransport.join(config)
        }
    }

    /** Leave and release the channel. Idempotent. */
    suspend fun stop() {
        mutex.withLock {
            transport?.leave()
            transport = null
            stateJob?.cancel()
            stateJob = null
            _sessionState.value = null
        }
    }

    /** Take the floor and begin transmitting (button / hardware-key down). */
    suspend fun startTransmitting(): PttTransmitResult =
        transport?.startTransmitting()
            ?: PttTransmitResult.Failed(IllegalStateException("No PTT session is active"))

    /** Release the floor and stop transmitting (button / hardware-key up). */
    suspend fun stopTransmitting() {
        transport?.stopTransmitting()
    }
}
