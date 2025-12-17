/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.previewutils.room

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import kotlinx.collections.immutable.persistentListOf

fun aRoomMember(
    userId: UserId = UserId("@alice:server.org"),
    displayName: String? = null,
    avatarUrl: String? = null,
    membership: RoomMembershipState = RoomMembershipState.JOIN,
    isNameAmbiguous: Boolean = false,
    powerLevel: Long = 0L,
    isIgnored: Boolean = false,
    role: RoomMember.Role = RoomMember.Role.User,
    membershipChangeReason: String? = null,
) = RoomMember(
    userId = userId,
    displayName = displayName,
    avatarUrl = avatarUrl,
    membership = membership,
    isNameAmbiguous = isNameAmbiguous,
    powerLevel = powerLevel,
    isIgnored = isIgnored,
    role = role,
    membershipChangeReason = membershipChangeReason,
)

fun aRoomMemberList() = persistentListOf(
    anAlice(),
    aBob(),
    aRoomMember(UserId("@carol:server.org"), "Carol"),
    aRoomMember(UserId("@david:server.org"), "David"),
    aRoomMember(UserId("@eve:server.org"), "Eve"),
    aRoomMember(UserId("@justin:server.org"), "Justin"),
    aRoomMember(UserId("@mallory:server.org"), "Mallory"),
    aRoomMember(UserId("@susie:server.org"), "Susie"),
    aVictor(),
    aWalter(),
)

fun anAlice() = aRoomMember(UserId("@alice:server.org"), "Alice", role = RoomMember.Role.Admin)
fun aBob() = aRoomMember(UserId("@bob:server.org"), "Bob", role = RoomMember.Role.Moderator)

fun aVictor() = aRoomMember(
    UserId("@victor:server.org"),
    "Victor",
    membership = RoomMembershipState.INVITE
)

fun aWalter() = aRoomMember(
    UserId("@walter:server.org"),
    "Walter",
    membership = RoomMembershipState.INVITE
)
