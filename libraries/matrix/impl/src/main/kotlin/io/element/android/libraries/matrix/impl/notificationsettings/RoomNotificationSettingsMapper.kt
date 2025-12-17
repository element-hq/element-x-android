/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.notificationsettings

import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import org.matrix.rustcomponents.sdk.RoomNotificationMode as RustRoomNotificationMode
import org.matrix.rustcomponents.sdk.RoomNotificationSettings as RustRoomNotificationSettings

object RoomNotificationSettingsMapper {
    fun map(roomNotificationSettings: RustRoomNotificationSettings): RoomNotificationSettings =
        RoomNotificationSettings(
            mode = mapMode(roomNotificationSettings.mode),
            isDefault = roomNotificationSettings.isDefault
        )

    fun mapMode(mode: RustRoomNotificationMode): RoomNotificationMode =
        when (mode) {
            RustRoomNotificationMode.ALL_MESSAGES -> RoomNotificationMode.ALL_MESSAGES
            RustRoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY -> RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
            RustRoomNotificationMode.MUTE -> RoomNotificationMode.MUTE
        }

    fun mapMode(mode: RoomNotificationMode): RustRoomNotificationMode =
        when (mode) {
            RoomNotificationMode.ALL_MESSAGES -> RustRoomNotificationMode.ALL_MESSAGES
            RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY -> RustRoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
            RoomNotificationMode.MUTE -> RustRoomNotificationMode.MUTE
        }
}
