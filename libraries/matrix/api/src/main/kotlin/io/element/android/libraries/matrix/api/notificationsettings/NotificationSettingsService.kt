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

package io.element.android.libraries.matrix.api.notificationsettings

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.MatrixRoomNotificationSettingsState
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import kotlinx.coroutines.flow.SharedFlow

interface NotificationSettingsService {
    /**
     * State of the current room notification settings flow ([MatrixRoomNotificationSettingsState.Unknown] if not started).
     */
    val notificationSettingsChangeFlow: SharedFlow<Unit>
    suspend fun getRoomNotificationSettings(roomId: RoomId, isEncrypted: Boolean, isOneToOne: Boolean): Result<RoomNotificationSettings>
    suspend fun getDefaultRoomNotificationMode(isEncrypted: Boolean, isOneToOne: Boolean): Result<RoomNotificationMode>
    suspend fun setDefaultRoomNotificationMode(isEncrypted: Boolean, mode: RoomNotificationMode, isOneToOne: Boolean): Result<Unit>
    suspend fun setRoomNotificationMode(roomId: RoomId, mode: RoomNotificationMode): Result<Unit>
    suspend fun restoreDefaultRoomNotificationMode(roomId: RoomId): Result<Unit>
    suspend fun muteRoom(roomId: RoomId): Result<Unit>
    suspend fun unmuteRoom(roomId: RoomId, isEncrypted: Boolean, isOneToOne: Boolean): Result<Unit>
    suspend fun isRoomMentionEnabled(): Result<Boolean>
    suspend fun setRoomMentionEnabled(enabled: Boolean): Result<Unit>
    suspend fun isCallEnabled(): Result<Boolean>
    suspend fun setCallEnabled(enabled: Boolean): Result<Unit>
    suspend fun isInviteForMeEnabled(): Result<Boolean>
    suspend fun setInviteForMeEnabled(enabled: Boolean): Result<Unit>
    suspend fun getRoomsWithUserDefinedRules(): Result<List<String>>
    suspend fun canHomeServerPushEncryptedEventsToDevice(): Result<Boolean>
}
