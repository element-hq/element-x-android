/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.ptt.api.PttChannelConfig
import io.element.android.features.ptt.api.PttSessionState
import io.element.android.features.ptt.api.PttTransmitResult
import io.element.android.features.ptt.api.PttTransport
import io.element.android.features.ptt.api.PttTransportFactory
import io.element.android.libraries.di.annotations.AppCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Session-host core: owns the single active [PttTransport] session, created from the injected
 * [PttTransportFactory] for the channel's [PttChannelConfig.transport]. App-scoped so a session
 * survives navigating out of the room; the foreground [io.element.android.features.ptt.impl.services.PttSessionHostService]
 * keeps the process alive while it's active. Transport-agnostic — it doesn't know Mumble from Element Call.
 */
@SingleIn(AppScope::class)
@Inject
class PttSessionController(
    private val transportFactory: PttTransportFactory,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
) {
    private val mutex = Mutex()

    @Volatile
    private var transport: PttTransport? = null
    private var stateJob: Job? = null

    private val _sessionState = MutableStateFlow<PttSessionState?>(null)

    /** The active session's state, or `null` when no channel is joined. */
    val sessionState: StateFlow<PttSessionState?> = _sessionState.asStateFlow()

    /** Create the transport for [config] and join it (fire-and-forget). Idempotent while live. */
    fun startSession(config: PttChannelConfig) {
        coroutineScope.launch {
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
    }

    /** Leave and release the channel (fire-and-forget). Idempotent. */
    fun stopSession() {
        coroutineScope.launch {
            mutex.withLock {
                transport?.leave()
                transport = null
                stateJob?.cancel()
                stateJob = null
                _sessionState.value = null
            }
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
