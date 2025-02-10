/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomMembershipDetails
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.preview.RoomPreviewInfo
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_ROOM_TOPIC
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.tests.testutils.lambda.lambdaError

fun aRoomPreview(
    sessionId: SessionId = A_SESSION_ID,
    info: RoomPreviewInfo = aRoomPreviewInfo(),
    declineInviteResult: () -> Result<Unit> = { lambdaError() },
    forgetRoomResult: () -> Result<Unit> = { lambdaError() },
) = FakeRoomPreview(
    sessionId = sessionId,
    info = info,
    declineInviteResult = declineInviteResult,
    forgetRoomResult = forgetRoomResult,
)

fun aRoomPreviewInfo(
    roomId: RoomId = A_ROOM_ID,
    name: String? = A_ROOM_NAME,
    topic: String? = A_ROOM_TOPIC,
    avatarUrl: String? = AN_AVATAR_URL,
    joinRule: JoinRule = JoinRule.Public,
    isSpace: Boolean = false,
    canonicalAlias: RoomAlias? = null,
    currentUserMembership: CurrentUserMembership? = null,
    numberOfJoinedMembers: Long = 1,
    isHistoryWorldReadable: Boolean = true,
) = RoomPreviewInfo(
    roomId = roomId,
    name = name,
    topic = topic,
    avatarUrl = avatarUrl,
    joinRule = joinRule,
    canonicalAlias = canonicalAlias,
    numberOfJoinedMembers = numberOfJoinedMembers,
    roomType = if (isSpace) RoomType.Space else RoomType.Room,
    isHistoryWorldReadable = isHistoryWorldReadable,
    membership = currentUserMembership,
)
