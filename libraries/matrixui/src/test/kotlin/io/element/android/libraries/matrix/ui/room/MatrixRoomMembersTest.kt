/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.room

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomMember
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MatrixRoomMembersTest {
    private val roomMember1 = aRoomMember(A_USER_ID)
    private val roomMember2 = aRoomMember(A_USER_ID_2)
    private val roomMember3 = aRoomMember(A_USER_ID_3)

    @Test
    fun `getDirectRoomMember emits other member for encrypted DM with 2 joined members`() = runTest {
        val matrixRoom = FakeMatrixRoom(
            sessionId = A_USER_ID,
            isEncrypted = true,
            isDirect = true,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            matrixRoom.getDirectRoomMember(
                MatrixRoomMembersState.Ready(persistentListOf(roomMember1, roomMember2))
            )
        }.test {
            assertThat(awaitItem().value).isEqualTo(roomMember2)
        }
    }

    @Test
    fun `getDirectRoomMember emit null if the room is not a dm`() = runTest {
        val matrixRoom = FakeMatrixRoom(
            sessionId = A_USER_ID,
            isEncrypted = true,
            isDirect = false,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            matrixRoom.getDirectRoomMember(
                MatrixRoomMembersState.Ready(persistentListOf(roomMember1, roomMember2))
            )
        }.test {
            assertThat(awaitItem().value).isNull()
        }
    }

    @Test
    fun `getDirectRoomMember emits other member even if the room is not encrypted`() = runTest {
        val matrixRoom = FakeMatrixRoom(
            sessionId = A_USER_ID,
            isEncrypted = false,
            isDirect = true,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            matrixRoom.getDirectRoomMember(
                MatrixRoomMembersState.Ready(persistentListOf(roomMember1, roomMember2))
            )
        }.test {
            assertThat(awaitItem().value).isEqualTo(roomMember2)
        }
    }

    @Test
    fun `getDirectRoomMember emit null if the room has only 1 member`() = runTest {
        val matrixRoom = FakeMatrixRoom(
            sessionId = A_USER_ID,
            isEncrypted = true,
            isDirect = true,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            matrixRoom.getDirectRoomMember(
                MatrixRoomMembersState.Ready(persistentListOf(roomMember1))
            )
        }.test {
            assertThat(awaitItem().value).isNull()
        }
    }

    @Test
    fun `getDirectRoomMember emit null if the room has only 3 members`() = runTest {
        val matrixRoom = FakeMatrixRoom(
            sessionId = A_USER_ID,
            isEncrypted = true,
            isDirect = true,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            matrixRoom.getDirectRoomMember(
                MatrixRoomMembersState.Ready(persistentListOf(roomMember1, roomMember2, roomMember3))
            )
        }.test {
            assertThat(awaitItem().value).isNull()
        }
    }

    @Test
    fun `getDirectRoomMember emit null if the other member is not active`() = runTest {
        val matrixRoom = FakeMatrixRoom(
            sessionId = A_USER_ID,
            isEncrypted = true,
            isDirect = true,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            matrixRoom.getDirectRoomMember(
                MatrixRoomMembersState.Ready(
                    persistentListOf(
                        roomMember1,
                        roomMember2.copy(membership = RoomMembershipState.BAN),
                    )
                )
            )
        }.test {
            assertThat(awaitItem().value).isNull()
        }
    }

    @Test
    fun `getDirectRoomMember emit the other member if there are 2 active members`() = runTest {
        val matrixRoom = FakeMatrixRoom(
            sessionId = A_USER_ID,
            isEncrypted = true,
            isDirect = true,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            matrixRoom.getDirectRoomMember(
                MatrixRoomMembersState.Ready(
                    persistentListOf(
                        roomMember1,
                        roomMember2,
                        roomMember3.copy(membership = RoomMembershipState.BAN),
                    )
                )
            )
        }.test {
            assertThat(awaitItem().value).isEqualTo(roomMember2)
        }
    }

    @Test
    fun `getCurrentRoomMember returns the current user`() = runTest {
        val matrixRoom = FakeMatrixRoom(sessionId = A_USER_ID)
        moleculeFlow(RecompositionMode.Immediate) {
            matrixRoom.getCurrentRoomMember(
                MatrixRoomMembersState.Ready(
                    persistentListOf(
                        roomMember1,
                        roomMember2,
                        roomMember3,
                    )
                )
            )
        }.test {
            assertThat(awaitItem().value).isEqualTo(roomMember1)
        }
    }

    @Test
    fun `getCurrentRoomMember returns null if the member is not found`() = runTest {
        val matrixRoom = FakeMatrixRoom(sessionId = A_USER_ID)
        moleculeFlow(RecompositionMode.Immediate) {
            matrixRoom.getCurrentRoomMember(
                MatrixRoomMembersState.Ready(
                    persistentListOf(
                        roomMember2,
                        roomMember3,
                    )
                )
            )
        }.test {
            assertThat(awaitItem().value).isNull()
        }
    }
}
