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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

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
    private val tones: PttTones,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
) {
    private val mutex = Mutex()

    @Volatile
    private var transport: PttTransport? = null
    private var stateJob: Job? = null

    private val _sessionState = MutableStateFlow<PttSessionState?>(null)

    /** The active session's state, or `null` when no channel is joined. */
    val sessionState: StateFlow<PttSessionState?> = _sessionState.asStateFlow()

    private val _isHearingEnabled = MutableStateFlow(false)

    /** Whether the user has opted in to hearing others (audio output). Off by default — silent. */
    val isHearingEnabled: StateFlow<Boolean> = _isHearingEnabled.asStateFlow()

    private val _isCovert = MutableStateFlow(false)

    /**
     * Covert/silent master override. When on, audio output is forced off and all cues (go/deny
     * tones) are suppressed regardless of [isHearingEnabled] — the "my phone must not make a sound"
     * switch.
     */
    val isCovert: StateFlow<Boolean> = _isCovert.asStateFlow()

    // Audio plays only when the user opted in AND covert mode is off.
    private fun effectiveAudioOutput(): Boolean = _isHearingEnabled.value && !_isCovert.value

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
                try {
                    newTransport.join(config)
                    // Joins silent; apply the current listen preference (still off unless the user opted in).
                    newTransport.setAudioOutputEnabled(effectiveAudioOutput())
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Exception) {
                    // A transport that fails to join must not crash the app-scoped session host; the
                    // failure is reflected in the transport's own state flow.
                    Timber.w(error, "PTT session join failed")
                }
            }
        }
    }

    /** Opt in/out of hearing others. Takes effect immediately on the live session (if any). */
    fun setHearingEnabled(enabled: Boolean) {
        _isHearingEnabled.value = enabled
        applyAudioOutput()
    }

    /** Toggle covert/silent mode. Enabling it silences output immediately, overriding [isHearingEnabled]. */
    fun setCovert(enabled: Boolean) {
        _isCovert.value = enabled
        applyAudioOutput()
    }

    private fun applyAudioOutput() {
        val enabled = effectiveAudioOutput()
        coroutineScope.launch { mutex.withLock { transport?.setAudioOutputEnabled(enabled) } }
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

    /**
     * Take the floor and begin transmitting (talk button / hardware-key down), playing the go/deny
     * cue. Fire-and-forget so both the UI and hardware-button callbacks can call it directly.
     */
    fun pressToTalk() {
        coroutineScope.launch {
            val result = transport?.startTransmitting()
                ?: PttTransmitResult.Failed(IllegalStateException("No PTT session is active"))
            // Suppress all audible cues in covert mode — the device must stay silent.
            if (!_isCovert.value) {
                when (result) {
                    PttTransmitResult.Granted -> tones.playFloorGranted()
                    is PttTransmitResult.Denied, is PttTransmitResult.Failed -> tones.playFloorDenied()
                }
            }
        }
    }

    /** Release the floor and stop transmitting (talk button / hardware-key up). Fire-and-forget. */
    fun releaseToTalk() {
        coroutineScope.launch { transport?.stopTransmitting() }
    }
}
