/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomHero
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomInfo
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoom
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomInfoExtTest {
    @Test
    fun `get non empty element Heroes`() {
        val result = aRustRoomInfo(
            isDm = true,
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
            isDm = true,
            heroes = listOf(aRustRoomHero(), aRustRoomHero())
        ).elementHeroes()
        assertThat(result).isEmpty()
    }

    @Test
    fun `not direct and element Heroes is empty`() {
        val result = aRustRoomInfo(
            isDm = false,
            heroes = listOf(aRustRoomHero())
        ).elementHeroes()
        assertThat(result).isEmpty()
    }

    @Test
    fun `roomInfoUpdates returns the current value and then updates`() = runTest {
        val roomInfo = aRustRoomInfo(isDm = true)
        val room = FakeFfiRoom(roomInfo = roomInfo, testScope = backgroundScope)
        val flow = room.roomInfoUpdates(dispatchers = testCoroutineDispatchers())

        val firstValue = flow.first()
        assertThat(firstValue).isEqualTo(roomInfo)

        val newRoomInfo = aRustRoomInfo(isDm = false)
        room.givenRoomInfoUpdate(newRoomInfo)

        val secondValue = flow.first()
        assertThat(secondValue).isEqualTo(newRoomInfo)
    }
}
