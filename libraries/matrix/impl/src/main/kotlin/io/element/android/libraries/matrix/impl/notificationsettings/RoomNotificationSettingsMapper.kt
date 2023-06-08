/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
}
