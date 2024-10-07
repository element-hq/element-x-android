/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

sealed interface RoomDetailsEvent {
    data object LeaveRoom : RoomDetailsEvent
    data object MuteNotification : RoomDetailsEvent
    data object UnmuteNotification : RoomDetailsEvent
    data class SetFavorite(val isFavorite: Boolean) : RoomDetailsEvent
}
