/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_ROOM_TOPIC
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.lambda.lambdaError

fun aMatrixRoom(
    roomId: RoomId = A_ROOM_ID,
    displayName: String = A_ROOM_NAME,
    rawName: String? = displayName,
    topic: String? = A_ROOM_TOPIC,
    avatarUrl: String? = AN_AVATAR_URL,
    isEncrypted: Boolean = true,
    isPublic: Boolean = true,
    isDirect: Boolean = false,
    joinRule: JoinRule? = null,
    notificationSettingsService: FakeNotificationSettingsService = FakeNotificationSettingsService(),
    emitRoomInfo: Boolean = false,
    canInviteResult: (UserId) -> Result<Boolean> = { lambdaError() },
    canBanResult: (UserId) -> Result<Boolean> = { lambdaError() },
    canSendStateResult: (UserId, StateEventType) -> Result<Boolean> = { _, _ -> lambdaError() },
    userDisplayNameResult: (UserId) -> Result<String?> = { lambdaError() },
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
    canBanResult = canBanResult,
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
                joinRule = joinRule,
            )
        )
    }
}
