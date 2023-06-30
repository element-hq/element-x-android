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

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.MatrixRoomNotificationSettingsState
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.NotificationSettings
import org.matrix.rustcomponents.sdk.NotificationSettingsDelegate
import org.matrix.rustcomponents.sdk.RoomNotificationMode

class RustNotificationSettingsService(
    private val client: Client,
    private val coroutineScope: CoroutineScope,
) : NotificationSettingsService, NotificationSettingsDelegate {

    private val notificationSettings: NotificationSettings = client.getNotificationSettings()
    private val _notificationSettingsStateFlow = MutableStateFlow<MatrixRoomNotificationSettingsState>(MatrixRoomNotificationSettingsState.Unknown)
    override val notificationSettingsStateFlow = _notificationSettingsStateFlow.asStateFlow()

    init {
        coroutineScope.launch { notificationSettings.setDelegate(this@RustNotificationSettingsService) }
    }

    override suspend fun getRoomNotificationSettings(roomId: RoomId): Result<RoomNotificationSettings> =
        runCatching {
            notificationSettings.getRoomNotificationMode(roomId.value).let(RoomNotificationSettingsMapper::map)
        }

    override suspend fun muteRoom(roomId: RoomId): Result<Unit> =
        runCatching {
            notificationSettings.setRoomNotificationMode(roomId.value, RoomNotificationMode.MUTE)
        }

    override suspend fun unmuteRoom(roomId: RoomId, isEncrypted: Boolean, membersCount: ULong) =
        runCatching {
            notificationSettings.unmuteRoom(roomId.value, isEncrypted, membersCount)
        }

    override fun notificationSettingsDidChange() {
        _notificationSettingsStateFlow.value = MatrixRoomNotificationSettingsState.ChangedNotificationSettings
    }
}
