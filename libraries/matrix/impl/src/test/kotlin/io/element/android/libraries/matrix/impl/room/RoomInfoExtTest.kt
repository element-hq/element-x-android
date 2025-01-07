/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomHero
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomInfo
import io.element.android.libraries.matrix.test.A_USER_ID
import org.junit.Test

class RoomInfoExtTest {
    @Test
    fun `get non empty element Heroes`() {
        val result = aRustRoomInfo(
            isDirect = true,
            activeMembersCount = 2uL,
            heroes = listOf(aRustRoomHero())
        ).elementHeroes()
        assertThat(result).isEqualTo(
            listOf(
                MatrixUser(
                    userId = UserId(A_USER_ID.value),
                    displayName = "displayName",
                    avatarUrl = "avatarUrl",
                )
            )
        )
    }

    @Test
    fun `too many heroes and element Heroes is empty`() {
        val result = aRustRoomInfo(
            isDirect = true,
            activeMembersCount = 2uL,
            heroes = listOf(aRustRoomHero(), aRustRoomHero())
        ).elementHeroes()
        assertThat(result).isEmpty()
    }

    @Test
    fun `not direct and element Heroes is empty`() {
        val result = aRustRoomInfo(
            isDirect = false,
            activeMembersCount = 2uL,
            heroes = listOf(aRustRoomHero())
        ).elementHeroes()
        assertThat(result).isEmpty()
    }

    @Test
    fun `too many members and element Heroes is empty`() {
        val result = aRustRoomInfo(
            isDirect = true,
            activeMembersCount = 3uL,
            heroes = listOf(aRustRoomHero())
        ).elementHeroes()
        assertThat(result).isEmpty()
    }
}
