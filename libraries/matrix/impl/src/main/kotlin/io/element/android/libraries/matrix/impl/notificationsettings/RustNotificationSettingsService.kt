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
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.NotificationSettings
import org.matrix.rustcomponents.sdk.NotificationSettingsDelegate
import org.matrix.rustcomponents.sdk.RoomNotificationMode as RustRoomNotificationMode

class RustNotificationSettingsService(
    private val client: Client,
) : NotificationSettingsService, NotificationSettingsDelegate {

    private val notificationSettings: NotificationSettings = client.getNotificationSettings()

    private val _notificationSettingsChangeFlow = MutableSharedFlow<Unit>()
    override val notificationSettingsChangeFlow: SharedFlow<Unit> = _notificationSettingsChangeFlow.asSharedFlow()

//    override val notificationSettingsChangeFlow = callbackFlow {
//        val delegate = object:NotificationSettingsDelegate {
//            override fun notificationSettingsDidChange() {
//                trySendBlocking(Unit)
//            }
//        }
//        send(Unit)
//        notificationSettings.setDelegate(delegate)
//        awaitClose {
//         //   notificationSettings.setDelegate(null)
//        }
//    }.buffer(Channel.UNLIMITED)

    init {
        notificationSettings.setDelegate(this)
    }

    override suspend fun getRoomNotificationSettings(roomId: RoomId): Result<RoomNotificationSettings> =
        runCatching {
            notificationSettings.getRoomNotificationMode(roomId.value).let(RoomNotificationSettingsMapper::map)
        }

    override suspend fun getDefaultRoomNotificationMode(isEncrypted: Boolean, membersCount: ULong): Result<RoomNotificationMode> =
        runCatching {
            notificationSettings.getDefaultRoomNotificationMode(isEncrypted, membersCount).let(RoomNotificationSettingsMapper::mapMode)
        }

    override suspend fun setRoomNotificationMode(roomId: RoomId, mode: RoomNotificationMode): Result<Unit> =
        runCatching {
            notificationSettings.setRoomNotificationMode(roomId.value, mode.let(RoomNotificationSettingsMapper::mapMode))
        }

    override suspend fun restoreDefaultRoomNotificationMode(roomId: RoomId): Result<Unit> =
        runCatching {
            notificationSettings.restoreDefaultRoomNotificationMode(roomId.value)
        }

    override suspend fun muteRoom(roomId: RoomId): Result<Unit> = setRoomNotificationMode(roomId, RoomNotificationMode.MUTE)

    override suspend fun unmuteRoom(roomId: RoomId, isEncrypted: Boolean, membersCount: ULong) =
        runCatching {
            notificationSettings.unmuteRoom(roomId.value, isEncrypted, membersCount)
        }

    override fun notificationSettingsDidChange() {
        _notificationSettingsChangeFlow.tryEmit(Unit)
    }
}
