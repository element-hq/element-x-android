/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.notificationsettings

import io.element.android.libraries.matrix.api.room.RoomNotificationMode

sealed interface RoomNotificationSettingsEvents {
    data class ChangeRoomNotificationMode(val mode: RoomNotificationMode) : RoomNotificationSettingsEvents
    data class SetNotificationMode(val isDefault: Boolean) : RoomNotificationSettingsEvents
    data object DeleteCustomNotification : RoomNotificationSettingsEvents
    data object ClearSetNotificationError : RoomNotificationSettingsEvents
    data object ClearRestoreDefaultError : RoomNotificationSettingsEvents
}
