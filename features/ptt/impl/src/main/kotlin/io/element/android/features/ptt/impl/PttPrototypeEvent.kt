/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

sealed interface PttPrototypeEvent {
    /** Join (or re-open) the room's Element Call audio session as the live PTT channel. */
    data object JoinPttChannel : PttPrototypeEvent

    /** Enable/disable PTT in this room (interim gate; see PttRoomService). */
    data class SetPttEnabled(val enabled: Boolean) : PttPrototypeEvent
}
