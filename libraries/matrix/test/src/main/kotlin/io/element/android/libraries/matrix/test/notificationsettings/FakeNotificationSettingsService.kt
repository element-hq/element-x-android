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
import io.element.android.libraries.matrix.test.A_ROOM_NOTIFICATION_SETTINGS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

class FakeNotificationSettingsService : NotificationSettingsService {
    private var _roomNotificationSettingsStateFlow = MutableStateFlow(Unit)
    private val muteRoomResult: Result<Unit> = Result.success(Unit)
    private val unmuteRoomResult: Result<Unit> = Result.success(Unit)
    private val setRoomNotificationMode: Result<Unit> = Result.success(Unit)
    private val restoreDefaultRoomNotificationMode: Result<Unit> = Result.success(Unit)
    private val getRoomNotificationSettingsResult: Result<RoomNotificationSettings> = Result.success(A_ROOM_NOTIFICATION_SETTINGS)
    private val getDefaultRoomNotificationMode: Result<RoomNotificationMode> = Result.success(A_ROOM_NOTIFICATION_MODE)
    override val notificationSettingsChangeFlow: SharedFlow<Unit>
        get() = _roomNotificationSettingsStateFlow

    override suspend fun getRoomNotificationSettings(roomId: RoomId, isEncrypted: Boolean, isOneToOne: Boolean): Result<RoomNotificationSettings> {
        return getRoomNotificationSettingsResult
    }

    override suspend fun getDefaultRoomNotificationMode(isEncrypted: Boolean, isOneToOne: Boolean): Result<RoomNotificationMode> {
        return getDefaultRoomNotificationMode
    }

    override suspend fun setDefaultRoomNotificationMode(isEncrypted: Boolean, mode: RoomNotificationMode, isOneToOne: Boolean): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun setRoomNotificationMode(roomId: RoomId, mode: RoomNotificationMode): Result<Unit> {
        return setRoomNotificationMode
    }

    override suspend fun restoreDefaultRoomNotificationMode(roomId: RoomId): Result<Unit> {
        return restoreDefaultRoomNotificationMode
    }

    override suspend fun muteRoom(roomId: RoomId): Result<Unit> {
        return muteRoomResult
    }

    override suspend fun unmuteRoom(roomId: RoomId, isEncrypted: Boolean, isOneToOne: Boolean): Result<Unit> {
        return unmuteRoomResult
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
