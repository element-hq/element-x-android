/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomdirectory

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.roomdirectory.RoomDescription
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.roomdirectory.aRoomDescription
import org.junit.Test
import org.matrix.rustcomponents.sdk.PublicRoomJoinRule
import org.matrix.rustcomponents.sdk.RoomDescription as RustRoomDescription

class RoomDescriptionMapperTest {
    @Test
    fun map() {
        assertThat(RoomDescriptionMapper().map(aRustRoomDescription())).isEqualTo(
            aRoomDescription(
                roomId = A_ROOM_ID,
                name = "name",
                topic = "topic",
                alias = A_ROOM_ALIAS,
                avatarUrl = "avatarUrl",
                joinRule = RoomDescription.JoinRule.PUBLIC,
                isWorldReadable = true,
                joinedMembers = 2L
            )
        )
    }

    @Test
    fun mapWithNullAlias() {
        assertThat(RoomDescriptionMapper().map(aRustRoomDescription().copy(alias = null)).alias).isNull()
    }

    @Test
    fun `map join rule`() {
        assertThat(PublicRoomJoinRule.PUBLIC.map()).isEqualTo(RoomDescription.JoinRule.PUBLIC)
        assertThat(PublicRoomJoinRule.KNOCK.map()).isEqualTo(RoomDescription.JoinRule.KNOCK)
        assertThat(null.map()).isEqualTo(RoomDescription.JoinRule.UNKNOWN)
    }
}

internal fun aRustRoomDescription(): RustRoomDescription {
    return RustRoomDescription(
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
