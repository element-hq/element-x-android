/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import org.matrix.rustcomponents.sdk.PublicRoomJoinRule
import org.matrix.rustcomponents.sdk.RoomDescription

internal fun aRustRoomDescription(
    roomId: String = A_ROOM_ID.value,
    name: String? = "name",
    topic: String? = "topic",
    alias: String? = A_ROOM_ALIAS.value,
    avatarUrl: String? = "avatarUrl",
    joinRule: PublicRoomJoinRule = PublicRoomJoinRule.PUBLIC,
    isWorldReadable: Boolean = true,
    joinedMembers: ULong = 2u,
): RoomDescription {
    return RoomDescription(
        roomId = roomId,
        name = name,
        topic = topic,
        alias = alias,
        avatarUrl = avatarUrl,
        joinRule = joinRule,
        isWorldReadable = isWorldReadable,
        joinedMembers = joinedMembers,
    )
}
