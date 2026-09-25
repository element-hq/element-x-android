/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.api

/**
 * Tells the ringing machinery what became of the call it is ringing for.
 *
 * There is one ring per incoming call, and it is owned by `ActiveCallManager` in
 * `features/call/impl` - but *whichever* call implementation answers has to stop it, cancel the
 * missed-call timeout and release the wake lock. The WebView path reaches the manager directly,
 * being in the same module; the native path is in another one, and this is the narrow slice of it
 * that anyone answering a call needs.
 */
interface RingingCallTracker {
    /**
     * The user is now in the call: stop ringing, take down the incoming-call UI, and cancel the
     * timeout that would otherwise post a missed call.
     */
    suspend fun onCallJoined(callData: CallData)

    /** The call is over. Clears the active call, and stops a ring that is still in progress. */
    suspend fun onCallEnded(callData: CallData)
}
