/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_ROOM_TOPIC
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.lambda.lambdaError

fun aRoom(
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    displayName: String = A_ROOM_NAME,
    rawName: String? = displayName,
    topic: String? = A_ROOM_TOPIC,
    avatarUrl: String? = AN_AVATAR_URL,
    canonicalAlias: RoomAlias? = A_ROOM_ALIAS,
    isEncrypted: Boolean = true,
    isPublic: Boolean = true,
    isDirect: Boolean = false,
    joinRule: JoinRule? = null,
    activeMemberCount: Long = 1,
    joinedMemberCount: Long = 1,
    invitedMemberCount: Long = 0,
    canInviteResult: (UserId) -> Result<Boolean> = { lambdaError() },
    canBanResult: (UserId) -> Result<Boolean> = { lambdaError() },
    canKickResult: (UserId) -> Result<Boolean> = { lambdaError() },
    canSendStateResult: (UserId, StateEventType) -> Result<Boolean> = { _, _ -> lambdaError() },
    userDisplayNameResult: (UserId) -> Result<String?> = { lambdaError() },
    userAvatarUrlResult: () -> Result<String?> = { lambdaError() },
    canUserJoinCallResult: (UserId) -> Result<Boolean> = { lambdaError() },
    getUpdatedMemberResult: (UserId) -> Result<RoomMember> = { lambdaError() },
    userRoleResult: () -> Result<RoomMember.Role> = { lambdaError() },
    setIsFavoriteResult: (Boolean) -> Result<Unit> = { lambdaError() },
) = FakeBaseRoom(
    sessionId = sessionId,
    roomId = roomId,
    canInviteResult = canInviteResult,
    canBanResult = canBanResult,
    canKickResult = canKickResult,
    canSendStateResult = canSendStateResult,
    userDisplayNameResult = userDisplayNameResult,
    userAvatarUrlResult = userAvatarUrlResult,
    canUserJoinCallResult = canUserJoinCallResult,
    getUpdatedMemberResult = getUpdatedMemberResult,
    userRoleResult = userRoleResult,
    setIsFavoriteResult = setIsFavoriteResult,
    initialRoomInfo = aRoomInfo(
        name = displayName,
        rawName = rawName,
        topic = topic,
        avatarUrl = avatarUrl,
        canonicalAlias = canonicalAlias,
        isDirect = isDirect,
        isPublic = isPublic,
        isEncrypted = isEncrypted,
        joinRule = joinRule,
        joinedMembersCount = joinedMemberCount,
        activeMembersCount = activeMemberCount,
        invitedMembersCount = invitedMemberCount,
    )
)

fun aJoinedRoom(
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    displayName: String = A_ROOM_NAME,
    rawName: String? = displayName,
    topic: String? = A_ROOM_TOPIC,
    avatarUrl: String? = AN_AVATAR_URL,
    canonicalAlias: RoomAlias? = A_ROOM_ALIAS,
    isEncrypted: Boolean = true,
    isPublic: Boolean = true,
    isDirect: Boolean = false,
    joinRule: JoinRule? = null,
    activeMemberCount: Long = 1,
    joinedMemberCount: Long = 1,
    invitedMemberCount: Long = 0,
    notificationSettingsService: FakeNotificationSettingsService = FakeNotificationSettingsService(),
    canInviteResult: (UserId) -> Result<Boolean> = { lambdaError() },
    canBanResult: (UserId) -> Result<Boolean> = { lambdaError() },
    canKickResult: (UserId) -> Result<Boolean> = { lambdaError() },
    canSendStateResult: (UserId, StateEventType) -> Result<Boolean> = { _, _ -> lambdaError() },
    userDisplayNameResult: (UserId) -> Result<String?> = { lambdaError() },
    userAvatarUrlResult: () -> Result<String?> = { lambdaError() },
    setNameResult: (String) -> Result<Unit> = { lambdaError() },
    setTopicResult: (String) -> Result<Unit> = { lambdaError() },
    updateAvatarResult: (String, ByteArray) -> Result<Unit> = { _, _ -> lambdaError() },
    removeAvatarResult: () -> Result<Unit> = { lambdaError() },
    canUserJoinCallResult: (UserId) -> Result<Boolean> = { lambdaError() },
    getUpdatedMemberResult: (UserId) -> Result<RoomMember> = { lambdaError() },
    userRoleResult: () -> Result<RoomMember.Role> = { lambdaError() },
    kickUserResult: (UserId, String?) -> Result<Unit> = { _, _ -> lambdaError() },
    banUserResult: (UserId, String?) -> Result<Unit> = { _, _ -> lambdaError() },
    unBanUserResult: (UserId, String?) -> Result<Unit> = { _, _ -> lambdaError() },
    updateCanonicalAliasResult: (RoomAlias?, List<RoomAlias>) -> Result<Unit> = { _, _ -> lambdaError() },
    publishRoomAliasInRoomDirectoryResult: (RoomAlias) -> Result<Boolean> = { lambdaError() },
    removeRoomAliasFromRoomDirectoryResult: (RoomAlias) -> Result<Boolean> = { lambdaError() },
    setIsFavoriteResult: (Boolean) -> Result<Unit> = { lambdaError() },
) = FakeJoinedRoom(
    roomNotificationSettingsService = notificationSettingsService,
    setNameResult = setNameResult,
    setTopicResult = setTopicResult,
    updateAvatarResult = updateAvatarResult,
    removeAvatarResult = removeAvatarResult,
    kickUserResult = kickUserResult,
    banUserResult = banUserResult,
    unBanUserResult = unBanUserResult,
    updateCanonicalAliasResult = updateCanonicalAliasResult,
    publishRoomAliasInRoomDirectoryResult = publishRoomAliasInRoomDirectoryResult,
    removeRoomAliasFromRoomDirectoryResult = removeRoomAliasFromRoomDirectoryResult,
    baseRoom = aRoom(
        sessionId = sessionId,
        roomId = roomId,
        canInviteResult = canInviteResult,
        canBanResult = canBanResult,
        canKickResult = canKickResult,
        canSendStateResult = canSendStateResult,
        userDisplayNameResult = userDisplayNameResult,
        userAvatarUrlResult = userAvatarUrlResult,
        canUserJoinCallResult = canUserJoinCallResult,
        getUpdatedMemberResult = getUpdatedMemberResult,
        userRoleResult = userRoleResult,
        setIsFavoriteResult = setIsFavoriteResult,
        displayName = displayName,
        rawName = rawName,
        topic = topic,
        avatarUrl = avatarUrl,
        canonicalAlias = canonicalAlias,
        isDirect = isDirect,
        isPublic = isPublic,
        isEncrypted = isEncrypted,
        joinRule = joinRule,
        joinedMemberCount = joinedMemberCount,
        activeMemberCount = activeMemberCount,
        invitedMemberCount = invitedMemberCount,
    )
)
