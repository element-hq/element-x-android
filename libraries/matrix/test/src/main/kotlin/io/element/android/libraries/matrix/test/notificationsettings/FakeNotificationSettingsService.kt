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

package io.element.android.libraries.matrix.test.notificationsettings

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import io.element.android.libraries.matrix.test.A_ROOM_NOTIFICATION_MODE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

class FakeNotificationSettingsService(
    initialMode: RoomNotificationMode = A_ROOM_NOTIFICATION_MODE,
    initialDefaultMode: RoomNotificationMode = A_ROOM_NOTIFICATION_MODE
) : NotificationSettingsService {
    private var _roomNotificationSettingsStateFlow = MutableStateFlow(Unit)
    private var defaultRoomNotificationMode: RoomNotificationMode = initialDefaultMode
    private var roomNotificationMode: RoomNotificationMode = initialMode
    override val notificationSettingsChangeFlow: SharedFlow<Unit>
        get() = _roomNotificationSettingsStateFlow

    override suspend fun getRoomNotificationSettings(roomId: RoomId, isEncrypted: Boolean, membersCount: Long): Result<RoomNotificationSettings> {
        return Result.success(RoomNotificationSettings(mode = roomNotificationMode, isDefault = roomNotificationMode == defaultRoomNotificationMode))
    }

    override suspend fun getDefaultRoomNotificationMode(isEncrypted: Boolean, membersCount: Long): Result<RoomNotificationMode> {
        return Result.success(defaultRoomNotificationMode)
    }

    override suspend fun setRoomNotificationMode(roomId: RoomId, mode: RoomNotificationMode): Result<Unit> {
        roomNotificationMode = mode
        _roomNotificationSettingsStateFlow.emit(Unit)
        return Result.success(Unit)
    }

    override suspend fun restoreDefaultRoomNotificationMode(roomId: RoomId): Result<Unit> {
        roomNotificationMode = defaultRoomNotificationMode
        _roomNotificationSettingsStateFlow.emit(Unit)
        return Result.success(Unit)
    }

    override suspend fun muteRoom(roomId: RoomId): Result<Unit> {
        return setRoomNotificationMode(roomId, RoomNotificationMode.MUTE)
    }

    override suspend fun unmuteRoom(roomId: RoomId, isEncrypted: Boolean, membersCount: Long): Result<Unit> {
        return restoreDefaultRoomNotificationMode(roomId)
    }

    override suspend fun isRoomMentionEnabled(): Result<Boolean> {
        return Result.success(false)
    }

    override suspend fun setRoomMentionEnabled(enabled: Boolean): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun isCallEnabled(): Result<Boolean> {
        return Result.success(false)
    }

    override suspend fun setCallEnabled(enabled: Boolean): Result<Unit> {
        return Result.success(Unit)
    }
}
