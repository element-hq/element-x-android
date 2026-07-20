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
}
