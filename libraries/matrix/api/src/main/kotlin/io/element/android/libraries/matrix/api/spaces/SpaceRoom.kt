/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.spaces

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

data class SpaceRoom(
    val rawName: String?,
    val displayName: String,
    val avatarUrl: String?,
    val canonicalAlias: RoomAlias?,
    val childrenCount: Int,
    val guestCanJoin: Boolean,
    val heroes: ImmutableList<MatrixUser>,
    val joinRule: JoinRule?,
    val numJoinedMembers: Int,
    val roomId: RoomId,
    val roomType: RoomType,
    val state: CurrentUserMembership?,
    val topic: String?,
    val worldReadable: Boolean,
    /**
     * The via parameters of the room.
     */
    val via: ImmutableList<String>,
    val isDirect: Boolean?,
) {
    val isSpace = roomType == RoomType.Space

    val visibility = SpaceRoomVisibility.fromJoinRule(joinRule)
}
