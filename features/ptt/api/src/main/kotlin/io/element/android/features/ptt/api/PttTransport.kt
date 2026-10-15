/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api

import kotlinx.coroutines.flow.StateFlow

/**
 * A pluggable half-duplex Push-to-Talk transport. Each implementation wraps exactly one backend —
 * Element Call (MatrixRTC) today; ZelloWork / Mumble later — and owns only the audio session and
 * floor control.
 *
 * It deliberately does NOT own the foreground service, the system overlay, tones, notifications, or
 * hardware/Bluetooth button input: those are transport-agnostic and live in the session host above
 * this seam. A transport that must keep a View attached to a live window to survive backgrounding
 * (Element Call's WebView) advertises that to the host through a separate capability interface, so
 * this API stays free of Android UI types.
 */
interface PttTransport {
    val type: PttTransportType

    /** Live session state for the UI. Emits an initial value immediately and never completes. */
    val state: StateFlow<PttSessionState>

    /**
     * Connect and join the channel described by [config]. Suspends until connected; throws on
     * failure. The transport joins **silent in both directions**: nothing transmits until
     * [startTransmitting], and incoming audio is not played until [setAudioOutputEnabled] is called
     * with `true`.
     */
    suspend fun join(config: PttChannelConfig)

    /**
     * Enable or disable audio output — i.e. whether incoming voice is played out of the speaker.
     *
     * The transport joins with output **disabled**: packets are still received, but with no playback
     * path they produce no sound. Enabling constructs the playback path; disabling tears it down.
     * Silence is therefore guaranteed by the *absence* of an output path, not by a volume mute — a
     * safety property the covert/silent mode relies on. Idempotent.
     */
    suspend fun setAudioOutputEnabled(enabled: Boolean)

    /** Leave the channel and release all resources. Idempotent; safe to call when not joined. */
    suspend fun leave()

    /**
     * Request the floor and begin transmitting (button / hardware-key down). Returns the outcome so
     * the caller can play the matching tone. Returns [PttTransmitResult.Granted] as a no-op if the
     * floor is already held locally.
     */
    suspend fun startTransmitting(): PttTransmitResult

    /** Release the floor and stop transmitting (button / hardware-key up). Idempotent. */
    suspend fun stopTransmitting()
}
