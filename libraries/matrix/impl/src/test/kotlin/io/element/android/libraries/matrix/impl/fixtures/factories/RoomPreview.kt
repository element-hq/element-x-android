/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import org.matrix.rustcomponents.sdk.RoomPreview

internal fun aRustRoomPreview(
    canonicalAlias: String? = A_ROOM_ALIAS.value,
    isJoined: Boolean = true,
    isInvited: Boolean = true,
    isPublic: Boolean = true,
    canKnock: Boolean = true,
): RoomPreview {
    return RoomPreview(
        roomId = A_ROOM_ID.value,
        canonicalAlias = canonicalAlias,
        name = "name",
        topic = "topic",
        avatarUrl = "avatarUrl",
        numJoinedMembers = 1u,
        roomType = null,
        isHistoryWorldReadable = true,
        isJoined = isJoined,
        isInvited = isInvited,
        isPublic = isPublic,
        canKnock = canKnock,
    )
}
