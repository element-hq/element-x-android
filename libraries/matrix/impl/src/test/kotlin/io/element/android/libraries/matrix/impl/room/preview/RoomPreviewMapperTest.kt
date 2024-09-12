/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.preview

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.api.room.preview.RoomPreview
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import org.junit.Test
import org.matrix.rustcomponents.sdk.RoomPreview as RustRoomPreview

class RoomPreviewMapperTest {
    @Test
    fun `map should map values 1`() {
        assertThat(
            RoomPreviewMapper.map(
                aRustRoomPreview(
                    isJoined = false,
                    isInvited = false,
                )
            )
        ).isEqualTo(
            RoomPreview(
                roomId = A_ROOM_ID,
                canonicalAlias = A_ROOM_ALIAS,
                name = "name",
                topic = "topic",
                avatarUrl = "avatarUrl",
                numberOfJoinedMembers = 1L,
                roomType = RoomType.Room,
                isHistoryWorldReadable = true,
                isJoined = false,
                isInvited = false,
                isPublic = true,
                canKnock = true,
            )
        )
    }

    @Test
    fun `map should map values 2`() {
        assertThat(
            RoomPreviewMapper.map(
                aRustRoomPreview(
                    canonicalAlias = null,
                    isPublic = false,
                    canKnock = false,
                )
            )
        ).isEqualTo(
            RoomPreview(
                roomId = A_ROOM_ID,
                canonicalAlias = null,
                name = "name",
                topic = "topic",
                avatarUrl = "avatarUrl",
                numberOfJoinedMembers = 1L,
                roomType = RoomType.Room,
                isHistoryWorldReadable = true,
                isJoined = true,
                isInvited = true,
                isPublic = false,
                canKnock = false,
            )
        )
    }
}

internal fun aRustRoomPreview(
    canonicalAlias: String? = A_ROOM_ALIAS.value,
    isJoined: Boolean = true,
    isInvited: Boolean = true,
    isPublic: Boolean = true,
    canKnock: Boolean = true,
): RustRoomPreview {
    return RustRoomPreview(
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
