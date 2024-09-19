/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import org.matrix.rustcomponents.sdk.PublicRoomJoinRule
import org.matrix.rustcomponents.sdk.RoomDescription

internal fun aRustRoomDescription(): RoomDescription {
    return RoomDescription(
        roomId = A_ROOM_ID.value,
        name = "name",
        topic = "topic",
        alias = A_ROOM_ALIAS.value,
        avatarUrl = "avatarUrl",
        joinRule = PublicRoomJoinRule.PUBLIC,
        isWorldReadable = true,
        joinedMembers = 2u
    )
}
