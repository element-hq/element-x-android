/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.analytics

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import org.junit.Test

class JoinedRoomExtKtTest {
    @Test
    fun `test room size mapping`() {
        mapOf(
            listOf(0L, 1L) to JoinedRoom.RoomSize.One,
            listOf(2L, 2L) to JoinedRoom.RoomSize.Two,
            listOf(3L, 10L) to JoinedRoom.RoomSize.ThreeToTen,
            listOf(11L, 100L) to JoinedRoom.RoomSize.ElevenToOneHundred,
            listOf(101L, 1000L) to JoinedRoom.RoomSize.OneHundredAndOneToAThousand,
            listOf(1001L, 2000L) to JoinedRoom.RoomSize.MoreThanAThousand
        ).forEach { (joinedMemberCounts, expectedRoomSize) ->
            joinedMemberCounts.forEach { joinedMemberCount ->
                assertThat(aMatrixRoom(joinedMemberCount = joinedMemberCount).toAnalyticsJoinedRoom(null))
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
    fun `test isDirect parameter mapping`() {
        assertThat(aMatrixRoom(isDirect = true).toAnalyticsJoinedRoom(null))
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
    fun `test isSpace parameter mapping`() {
        assertThat(aMatrixRoom(isSpace = true).toAnalyticsJoinedRoom(null))
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
    fun `test trigger parameter mapping`() {
        assertThat(aMatrixRoom().toAnalyticsJoinedRoom(JoinedRoom.Trigger.Invite))
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
    ): MatrixRoom {
        return FakeMatrixRoom(
            isDirect = isDirect,
            isSpace = isSpace,
            joinedMemberCount = joinedMemberCount
        )
    }
}
