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

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.NotificationSettings
import org.matrix.rustcomponents.sdk.NotificationSettingsDelegate

class RustNotificationSettingsService(
    private val notificationSettings: NotificationSettings,
    private val dispatchers: CoroutineDispatchers,
) : NotificationSettingsService {

    private val _notificationSettingsChangeFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val notificationSettingsChangeFlow: SharedFlow<Unit> = _notificationSettingsChangeFlow.asSharedFlow()

    private var notificationSettingsDelegate = object : NotificationSettingsDelegate {
        override fun settingsDidChange() {
            _notificationSettingsChangeFlow.tryEmit(Unit)
        }
    }

    init {
        notificationSettings.setDelegate(notificationSettingsDelegate)
    }

    override suspend fun getRoomNotificationSettings(roomId: RoomId, isEncrypted: Boolean, isOneToOne: Boolean): Result<RoomNotificationSettings> = withContext(
        dispatchers.io
    ) {
        runCatching {
            notificationSettings.getRoomNotificationSettingsBlocking(roomId.value, isEncrypted, isOneToOne).let(RoomNotificationSettingsMapper::map)
        }
    }

    override suspend fun getDefaultRoomNotificationMode(isEncrypted: Boolean, isOneToOne: Boolean): Result<RoomNotificationMode> = withContext(dispatchers.io) {
        runCatching {
            notificationSettings.getDefaultRoomNotificationModeBlocking(isEncrypted, isOneToOne).let(RoomNotificationSettingsMapper::mapMode)
        }
    }

    override suspend fun setDefaultRoomNotificationMode(
        isEncrypted: Boolean,
        mode: RoomNotificationMode,
        isOneToOne: Boolean
    ): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            notificationSettings.setDefaultRoomNotificationModeBlocking(isEncrypted, isOneToOne, mode.let(RoomNotificationSettingsMapper::mapMode))
        }
    }

    override suspend fun setRoomNotificationMode(roomId: RoomId, mode: RoomNotificationMode): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            notificationSettings.setRoomNotificationModeBlocking(roomId.value, mode.let(RoomNotificationSettingsMapper::mapMode))
        }
    }

    override suspend fun restoreDefaultRoomNotificationMode(roomId: RoomId): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            notificationSettings.restoreDefaultRoomNotificationModeBlocking(roomId.value)
        }
    }

    override suspend fun muteRoom(roomId: RoomId): Result<Unit> = setRoomNotificationMode(roomId, RoomNotificationMode.MUTE)

    override suspend fun unmuteRoom(roomId: RoomId, isEncrypted: Boolean, isOneToOne: Boolean) = withContext(dispatchers.io) {
        runCatching {
            notificationSettings.unmuteRoomBlocking(roomId.value, isEncrypted, isOneToOne)
        }
    }

    override suspend fun isRoomMentionEnabled(): Result<Boolean> = withContext(dispatchers.io) {
        runCatching {
            notificationSettings.isRoomMentionEnabledBlocking()
        }
    }

    override suspend fun setRoomMentionEnabled(enabled: Boolean): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            notificationSettings.setRoomMentionEnabledBlocking(enabled)
        }
    }

    override suspend fun isCallEnabled(): Result<Boolean> = withContext(dispatchers.io) {
        runCatching {
            notificationSettings.isCallEnabledBlocking()
        }
    }

    override suspend fun setCallEnabled(enabled: Boolean): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            notificationSettings.setCallEnabledBlocking(enabled)
        }
    }
}
