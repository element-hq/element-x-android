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
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.NotificationSettingsDelegate
import org.matrix.rustcomponents.sdk.RoomNotificationMode

class RustNotificationSettingsService(
    private val client: Client,
) : NotificationSettingsService, NotificationSettingsDelegate {

    private val _roomNotificationSettingsStateFlow = MutableStateFlow<MatrixRoomNotificationSettingsState>(MatrixRoomNotificationSettingsState.Unknown)
    override val roomNotificationSettingsStateFlow = _roomNotificationSettingsStateFlow.asStateFlow()
    override suspend fun getRoomNotificationMode(roomId: RoomId): Result<RoomNotificationSettings> =
        runCatching {
            client.getNotificationSettings().getRoomNotificationMode(roomId.value).let(RoomNotificationSettingsMapper::map)
        }

    override suspend fun muteRoom(roomId: RoomId): Result<Unit> =
        runCatching {
            client.getNotificationSettings().setRoomNotificationMode(roomId.value, RoomNotificationMode.MUTE)
        }

    override suspend fun unmuteRoom(roomId: RoomId) =
        runCatching {
            client.getNotificationSettings().unmuteRoom(roomId.value)
        }

    override fun notificationSettingsDidChange() {
        _roomNotificationSettingsStateFlow.value = MatrixRoomNotificationSettingsState.ChangedNotificationSettings
    }
}
