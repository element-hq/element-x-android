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
import org.matrix.rustcomponents.sdk.JoinRule
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.RoomPreviewInfo
import org.matrix.rustcomponents.sdk.RoomType

internal fun aRustRoomPreviewInfo(
    canonicalAlias: String? = A_ROOM_ALIAS.value,
    membership: Membership? = Membership.JOINED,
    joinRule: JoinRule = JoinRule.Public,
): RoomPreviewInfo {
    return RoomPreviewInfo(
        roomId = A_ROOM_ID.value,
        canonicalAlias = canonicalAlias,
        name = "name",
        topic = "topic",
        avatarUrl = "avatarUrl",
        numJoinedMembers = 1u,
        numActiveMembers = 1u,
        isDirect = false,
        roomType = RoomType.Room,
        isHistoryWorldReadable = true,
        membership = membership,
        joinRule = joinRule,
        heroes = null,
    )
}
