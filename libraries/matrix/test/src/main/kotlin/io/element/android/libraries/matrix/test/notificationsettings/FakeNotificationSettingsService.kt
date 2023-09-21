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
    initialRoomMode: RoomNotificationMode = A_ROOM_NOTIFICATION_MODE,
    initialGroupDefaultMode: RoomNotificationMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
    initialEncryptedGroupDefaultMode: RoomNotificationMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
    initialOneToOneDefaultMode: RoomNotificationMode = RoomNotificationMode.ALL_MESSAGES,
    initialEncryptedOneToOneDefaultMode: RoomNotificationMode = RoomNotificationMode.ALL_MESSAGES,
) : NotificationSettingsService {
    private var _notificationSettingsStateFlow = MutableStateFlow(Unit)
    private var defaultGroupRoomNotificationMode: RoomNotificationMode = initialGroupDefaultMode
    private var defaultEncryptedGroupRoomNotificationMode: RoomNotificationMode = initialEncryptedGroupDefaultMode
    private var defaultOneToOneRoomNotificationMode: RoomNotificationMode = initialOneToOneDefaultMode
    private var defaultEncryptedOneToOneRoomNotificationMode: RoomNotificationMode = initialEncryptedOneToOneDefaultMode
    private var roomNotificationMode: RoomNotificationMode = initialRoomMode
    private var callNotificationsEnabled = false
    private var atRoomNotificationsEnabled = false
    override val notificationSettingsChangeFlow: SharedFlow<Unit>
        get() = _notificationSettingsStateFlow

    override suspend fun getRoomNotificationSettings(roomId: RoomId, isEncrypted: Boolean, isOneToOne: Boolean): Result<RoomNotificationSettings> {
        return Result.success(
            RoomNotificationSettings(
                mode = roomNotificationMode,
                isDefault = roomNotificationMode == defaultEncryptedGroupRoomNotificationMode
            )
        )
    }

    override suspend fun getDefaultRoomNotificationMode(isEncrypted: Boolean, isOneToOne: Boolean): Result<RoomNotificationMode> {
        return if (isOneToOne) {
            if (isEncrypted) {
                Result.success(defaultEncryptedOneToOneRoomNotificationMode)
            } else {
                Result.success(defaultOneToOneRoomNotificationMode)
            }
        } else {
            if (isEncrypted) {
                Result.success(defaultEncryptedGroupRoomNotificationMode)
            } else {
                Result.success(defaultGroupRoomNotificationMode)
            }
        }
    }

    override suspend fun setDefaultRoomNotificationMode(isEncrypted: Boolean, mode: RoomNotificationMode, isOneToOne: Boolean): Result<Unit> {
        if (isOneToOne) {
            if (isEncrypted) {
                defaultEncryptedOneToOneRoomNotificationMode = mode
            } else {
                defaultOneToOneRoomNotificationMode = mode
            }
        } else {
            if (isEncrypted) {
                defaultEncryptedGroupRoomNotificationMode = mode
            } else {
                defaultGroupRoomNotificationMode = mode
            }
        }
        _notificationSettingsStateFlow.emit(Unit)
        return Result.success(Unit)
    }

    override suspend fun setRoomNotificationMode(roomId: RoomId, mode: RoomNotificationMode): Result<Unit> {
        roomNotificationMode = mode
        _notificationSettingsStateFlow.emit(Unit)
        return Result.success(Unit)
    }

    override suspend fun restoreDefaultRoomNotificationMode(roomId: RoomId): Result<Unit> {
        roomNotificationMode = defaultEncryptedGroupRoomNotificationMode
        _notificationSettingsStateFlow.emit(Unit)
        return Result.success(Unit)
    }

    override suspend fun muteRoom(roomId: RoomId): Result<Unit> {
        return setRoomNotificationMode(roomId, RoomNotificationMode.MUTE)
    }

    override suspend fun unmuteRoom(roomId: RoomId, isEncrypted: Boolean, isOneToOne: Boolean): Result<Unit> {
        return restoreDefaultRoomNotificationMode(roomId)
    }

    override suspend fun isRoomMentionEnabled(): Result<Boolean> {
        return Result.success(atRoomNotificationsEnabled)
    }

    override suspend fun setRoomMentionEnabled(enabled: Boolean): Result<Unit> {
        atRoomNotificationsEnabled = enabled
        return Result.success(Unit)
    }

    override suspend fun isCallEnabled(): Result<Boolean> {
        return Result.success(callNotificationsEnabled)
    }

    override suspend fun setCallEnabled(enabled: Boolean): Result<Unit> {
        callNotificationsEnabled = enabled
        return Result.success(Unit)
    }
}
