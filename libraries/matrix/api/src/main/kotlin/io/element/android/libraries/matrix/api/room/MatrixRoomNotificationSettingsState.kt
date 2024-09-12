/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

sealed interface MatrixRoomNotificationSettingsState {
    data object Unknown : MatrixRoomNotificationSettingsState
    data class Pending(val prevRoomNotificationSettings: RoomNotificationSettings? = null) : MatrixRoomNotificationSettingsState
    data class Error(val failure: Throwable, val prevRoomNotificationSettings: RoomNotificationSettings? = null) : MatrixRoomNotificationSettingsState
    data class Ready(val roomNotificationSettings: RoomNotificationSettings) : MatrixRoomNotificationSettingsState
}

fun MatrixRoomNotificationSettingsState.roomNotificationSettings(): RoomNotificationSettings? {
    return when (this) {
        is MatrixRoomNotificationSettingsState.Ready -> roomNotificationSettings
        is MatrixRoomNotificationSettingsState.Pending -> prevRoomNotificationSettings
        is MatrixRoomNotificationSettingsState.Error -> prevRoomNotificationSettings
        else -> null
    }
}
