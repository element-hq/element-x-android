/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api

import io.element.android.libraries.matrix.api.core.UserId

/**
 * Outcome of a request to take the floor and start transmitting, so the caller can play the right
 * audio cue: a "go" chirp on [Granted], a "deny" tone on [Denied] or [Failed] (REQ-780/781).
 */
sealed interface PttTransmitResult {
    /** The floor was acquired; the microphone is now transmitting. */
    data object Granted : PttTransmitResult

    /** Someone else currently holds the floor (half-duplex); [currentSpeaker] is them if known. */
    data class Denied(val currentSpeaker: UserId?) : PttTransmitResult

    /** The attempt failed for a technical reason (not connected, transport error, …). */
    data class Failed(val cause: Throwable) : PttTransmitResult
}
