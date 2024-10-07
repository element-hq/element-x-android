/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
