/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.analytics

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import kotlinx.coroutines.test.runTest
import org.junit.Test

class JoinedRoomExtKtTest {
    @Test
    fun `test room size mapping`() = runTest {
        mapOf(
            listOf(0L, 1L) to JoinedRoom.RoomSize.One,
            listOf(2L, 2L) to JoinedRoom.RoomSize.Two,
            listOf(3L, 10L) to JoinedRoom.RoomSize.ThreeToTen,
            listOf(11L, 100L) to JoinedRoom.RoomSize.ElevenToOneHundred,
            listOf(101L, 1000L) to JoinedRoom.RoomSize.OneHundredAndOneToAThousand,
            listOf(1001L, 2000L) to JoinedRoom.RoomSize.MoreThanAThousand
        ).forEach { (joinedMemberCounts, expectedRoomSize) ->
            joinedMemberCounts.forEach { joinedMemberCount ->
                val room = aMatrixRoom().apply {
                    givenRoomInfo(aRoomInfo(joinedMembersCount = joinedMemberCount))
                }
                assertThat(room.toAnalyticsJoinedRoom(null))
                    .isEqualTo(
                        JoinedRoom(
                            isDM = false,
                            isSpace = false,
                            roomSize = expectedRoomSize,
                            trigger = null
                        )
                    )
            }
        }
    }

    @Test
    fun `test isDirect parameter mapping`() = runTest {
        val room = aMatrixRoom().apply { givenRoomInfo(aRoomInfo(isDirect = true)) }
        assertThat(room.toAnalyticsJoinedRoom(null))
            .isEqualTo(
                JoinedRoom(
                    isDM = true,
                    isSpace = false,
                    roomSize = JoinedRoom.RoomSize.One,
                    trigger = null
                )
            )
    }

    @Test
    fun `test isSpace parameter mapping`() = runTest {
        val room = aMatrixRoom().apply { givenRoomInfo(aRoomInfo(isSpace = true)) }
        assertThat(room.toAnalyticsJoinedRoom(null))
            .isEqualTo(
                JoinedRoom(
                    isDM = false,
                    isSpace = true,
                    roomSize = JoinedRoom.RoomSize.One,
                    trigger = null
                )
            )
    }

    @Test
    fun `test trigger parameter mapping`() = runTest {
        val room = aMatrixRoom().apply { givenRoomInfo(aRoomInfo(isDirect = false, isSpace = false)) }
        assertThat(room.toAnalyticsJoinedRoom(JoinedRoom.Trigger.Invite))
            .isEqualTo(
                JoinedRoom(
                    isDM = false,
                    isSpace = false,
                    roomSize = JoinedRoom.RoomSize.One,
                    trigger = JoinedRoom.Trigger.Invite
                )
            )
    }

    private fun aMatrixRoom(
        isDirect: Boolean = false,
        isSpace: Boolean = false,
        joinedMemberCount: Long = 0
    ): FakeMatrixRoom {
        return FakeMatrixRoom(
            isDirect = isDirect,
            isSpace = isSpace,
            joinedMemberCount = joinedMemberCount,
        )
    }
}
