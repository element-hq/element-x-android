/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import org.matrix.rustcomponents.sdk.JoinRule
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.RoomHero
import org.matrix.rustcomponents.sdk.RoomType
import org.matrix.rustcomponents.sdk.SpaceRoom

fun aRustSpaceRoom(
    roomId: RoomId = A_ROOM_ID,
    isDirect: Boolean = false,
    canonicalAlias: String? = null,
    rawName: String? = null,
    displayName: String = "",
    topic: String? = null,
    avatarUrl: String? = null,
    roomType: RoomType = RoomType.Space,
    numJoinedMembers: ULong = 0uL,
    joinRule: JoinRule? = null,
    worldReadable: Boolean? = null,
    guestCanJoin: Boolean = false,
    childrenCount: ULong = 0uL,
    state: Membership? = null,
    heroes: List<RoomHero> = emptyList(),
) = SpaceRoom(
    roomId = roomId.value,
    isDirect = isDirect,
    canonicalAlias = canonicalAlias,
    rawName = rawName,
    displayName = displayName,
    topic = topic,
    avatarUrl = avatarUrl,
    roomType = roomType,
    numJoinedMembers = numJoinedMembers,
    joinRule = joinRule,
    worldReadable = worldReadable,
    guestCanJoin = guestCanJoin,
    childrenCount = childrenCount,
    state = state,
    heroes = heroes,
    via = emptyList()
)
