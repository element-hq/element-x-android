/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

sealed interface PttPrototypeEvent {
    /** Create the transport for this room's channel and join it (via [PttSessionController]). */
    data object JoinPttChannel : PttPrototypeEvent

    /** Leave and release the channel. */
    data object LeavePttChannel : PttPrototypeEvent

    /** Take the floor and start transmitting (talk button / hardware-key down). */
    data object StartTransmitting : PttPrototypeEvent

    /** Release the floor and stop transmitting (talk button / hardware-key up). */
    data object StopTransmitting : PttPrototypeEvent

    /** Enable/disable PTT in this room (interim gate; see PttRoomService). */
    data class SetPttEnabled(val enabled: Boolean) : PttPrototypeEvent

    /** Open the system settings screen to grant the "draw over other apps" permission. */
    data object GrantOverlayPermission : PttPrototypeEvent

    /** DEBUG: fire a full-screen "join push-to-talk" alert after a short delay (to test lock-screen). */
    data object SimulateLockScreenAlert : PttPrototypeEvent

    /** Open settings to allow full-screen-intent notifications (the lock-screen alert). */
    data object GrantFullScreenIntent : PttPrototypeEvent

    /** User-opt-in toggle for the battery-optimization exemption (request dialog, or settings to revoke). */
    data object ToggleBatteryOptimizationExemption : PttPrototypeEvent
}
