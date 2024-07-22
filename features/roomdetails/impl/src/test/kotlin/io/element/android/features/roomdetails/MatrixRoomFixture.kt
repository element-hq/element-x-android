/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomdetails

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.lambda.lambdaError

fun aMatrixRoom(
    roomId: RoomId = A_ROOM_ID,
    displayName: String = A_ROOM_NAME,
    rawName: String? = displayName,
    topic: String? = "A topic",
    avatarUrl: String? = "https://matrix.org/avatar.jpg",
    isEncrypted: Boolean = true,
    isPublic: Boolean = true,
    isDirect: Boolean = false,
    notificationSettingsService: FakeNotificationSettingsService = FakeNotificationSettingsService(),
    emitRoomInfo: Boolean = false,
    canInviteResult: (UserId) -> Result<Boolean> = { lambdaError() },
    canSendStateResult: (UserId, StateEventType) -> Result<Boolean> = { _, _ -> lambdaError() },
    userDisplayNameResult: () -> Result<String?> = { lambdaError() },
    userAvatarUrlResult: () -> Result<String?> = { lambdaError() },
    setNameResult: (String) -> Result<Unit> = { lambdaError() },
    setTopicResult: (String) -> Result<Unit> = { lambdaError() },
    updateAvatarResult: (String, ByteArray) -> Result<Unit> = { _, _ -> lambdaError() },
    removeAvatarResult: () -> Result<Unit> = { lambdaError() },
    canUserJoinCallResult: (UserId) -> Result<Boolean> = { lambdaError() },
    getUpdatedMemberResult: (UserId) -> Result<RoomMember> = { lambdaError() },
) = FakeMatrixRoom(
    roomId = roomId,
    displayName = displayName,
    topic = topic,
    avatarUrl = avatarUrl,
    isEncrypted = isEncrypted,
    isPublic = isPublic,
    isDirect = isDirect,
    notificationSettingsService = notificationSettingsService,
    canInviteResult = canInviteResult,
    canSendStateResult = canSendStateResult,
    userDisplayNameResult = userDisplayNameResult,
    userAvatarUrlResult = userAvatarUrlResult,
    setNameResult = setNameResult,
    setTopicResult = setTopicResult,
    updateAvatarResult = updateAvatarResult,
    removeAvatarResult = removeAvatarResult,
    canUserJoinCallResult = canUserJoinCallResult,
    getUpdatedMemberResult = getUpdatedMemberResult,
).apply {
    if (emitRoomInfo) {
        givenRoomInfo(
            aRoomInfo(
                name = displayName,
                rawName = rawName,
                topic = topic,
                avatarUrl = avatarUrl,
                isDirect = isDirect,
                isPublic = isPublic,
            )
        )
    }
}
