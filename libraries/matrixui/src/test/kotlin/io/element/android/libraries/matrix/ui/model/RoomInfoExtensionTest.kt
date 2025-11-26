/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.model

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.defaultRoomPowerLevelValues
import kotlinx.collections.immutable.toImmutableMap
import org.junit.Test

class RoomInfoExtensionTest {
    @Test
    fun `roleOf returns Owner for creator with privilegedCreatorRole true`() {
        val roomInfo = aRoomInfo(
            privilegedCreatorRole = true,
            roomCreators = listOf(A_USER_ID),
        )
        assertThat(roomInfo.roleOf(A_USER_ID)).isEqualTo(RoomMember.Role.Owner(isCreator = true))
    }

    @Test
    fun `roleOf returns User for not creator with privilegedCreatorRole true`() {
        val roomInfo = aRoomInfo(
            privilegedCreatorRole = true,
            roomCreators = listOf(A_USER_ID),
        )
        assertThat(roomInfo.roleOf(A_USER_ID_2)).isEqualTo(RoomMember.Role.User)
    }

    @Test
    fun `roleOf returns User for creator with privilegedCreatorRole false`() {
        val roomInfo = aRoomInfo(
            privilegedCreatorRole = false,
            roomCreators = listOf(A_USER_ID),
        )
        assertThat(roomInfo.roleOf(A_USER_ID)).isEqualTo(RoomMember.Role.User)
    }

    @Test
    fun `roleOf returns role from the power level`() {
        val roomInfo = aRoomInfo(
            privilegedCreatorRole = false,
            roomPowerLevels = RoomPowerLevels(
                values = defaultRoomPowerLevelValues(),
                users = mapOf(
                    A_USER_ID to 100L, // Admin
                    A_USER_ID_2 to 50L, // Moderator
                    A_USER_ID_3 to 0L, // User
                ).toImmutableMap(),
            ),
            roomCreators = listOf(A_USER_ID),
        )
        assertThat(roomInfo.roleOf(A_USER_ID)).isEqualTo(RoomMember.Role.Admin)
        assertThat(roomInfo.roleOf(A_USER_ID_2)).isEqualTo(RoomMember.Role.Moderator)
        assertThat(roomInfo.roleOf(A_USER_ID_3)).isEqualTo(RoomMember.Role.User)
    }

    @Test
    fun `roleOf returns User when the power level is null`() {
        val roomInfo = aRoomInfo(
            privilegedCreatorRole = false,
            roomPowerLevels = null,
            roomCreators = listOf(A_USER_ID),
        )
        assertThat(roomInfo.roleOf(A_USER_ID)).isEqualTo(RoomMember.Role.User)
        assertThat(roomInfo.roleOf(A_USER_ID_2)).isEqualTo(RoomMember.Role.User)
        assertThat(roomInfo.roleOf(A_USER_ID_3)).isEqualTo(RoomMember.Role.User)
    }
}
