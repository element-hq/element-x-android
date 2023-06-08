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
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import io.element.android.libraries.matrix.test.A_ROOM_NOTIFICATION_SETTINGS

class FakeNotificationSettingsService : NotificationSettingsService {
    private val muteRoomResult: Result<Unit> = Result.success(Unit)
    private val unmuteRoomResult: Result<Unit> = Result.success(Unit)
    private val getRoomNotificationSettingsResult: Result<RoomNotificationSettings> = Result.success(A_ROOM_NOTIFICATION_SETTINGS)

    override suspend fun getRoomNotificationMode(roomId: RoomId): Result<RoomNotificationSettings> {
        return getRoomNotificationSettingsResult
    }

    override suspend fun muteRoom(roomId: RoomId): Result<Unit> {
        return muteRoomResult
    }

    override suspend fun unmuteRoom(roomId: RoomId): Result<Unit> {
        return unmuteRoomResult
    }
}
