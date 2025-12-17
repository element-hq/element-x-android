/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

sealed interface RoomNotificationSettingsState {
    data object Unknown : RoomNotificationSettingsState
    data class Pending(val prevRoomNotificationSettings: RoomNotificationSettings? = null) : RoomNotificationSettingsState
    data class Error(val failure: Throwable, val prevRoomNotificationSettings: RoomNotificationSettings? = null) : RoomNotificationSettingsState
    data class Ready(val roomNotificationSettings: RoomNotificationSettings) : RoomNotificationSettingsState
}

fun RoomNotificationSettingsState.roomNotificationSettings(): RoomNotificationSettings? {
    return when (this) {
        is RoomNotificationSettingsState.Ready -> roomNotificationSettings
        is RoomNotificationSettingsState.Pending -> prevRoomNotificationSettings
        is RoomNotificationSettingsState.Error -> prevRoomNotificationSettings
        else -> null
    }
}
