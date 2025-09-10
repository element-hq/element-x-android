/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.previewutils.room

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.api.user.MatrixUser

fun aSpaceRoom(
    name: String? = "Space name",
    avatarUrl: String? = null,
    canonicalAlias: RoomAlias? = null,
    childrenCount: Int = 0,
    guestCanJoin: Boolean = false,
    heroes: List<MatrixUser> = emptyList(),
    joinRule: JoinRule? = null,
    numJoinedMembers: Int = 0,
    roomId: RoomId = RoomId("!roomId:example.com"),
    roomType: RoomType = RoomType.Space,
    state: CurrentUserMembership? = null,
    topic: String? = null,
    worldReadable: Boolean = false,
) = SpaceRoom(
    name = name,
    avatarUrl = avatarUrl,
    canonicalAlias = canonicalAlias,
    childrenCount = childrenCount,
    guestCanJoin = guestCanJoin,
    heroes = heroes,
    joinRule = joinRule,
    numJoinedMembers = numJoinedMembers,
    roomId = roomId,
    roomType = roomType,
    state = state,
    topic = topic,
    worldReadable = worldReadable
)
