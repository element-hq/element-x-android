/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.backfill

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.tombstone.SuccessorRoom
import io.element.android.libraries.matrix.api.roomlist.LatestEventValue
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.test.room.aRemoteLatestEvent
import io.element.android.libraries.matrix.test.room.aRoomSummary
import org.junit.Test

class SearchBackfillPlannerTest {
    @Test
    fun `rooms are ordered most recently active first`() {
        // Given SHUFFLED input, so a pass cannot come from the input already being in order. The
        // room list replays SDK diffs verbatim and applies no app-side sort, so the ordering has to
        // be the planner's own doing.
        val summaries = listOf(
            aRoom("!old:server", timestamp = 100L),
            aRoom("!newest:server", timestamp = 900L),
            aRoom("!middle:server", timestamp = 500L),
        )

        val plan = planSearchBackfill(summaries)

        assertThat(plan).isEqualTo(
            listOf(RoomId("!newest:server"), RoomId("!middle:server"), RoomId("!old:server"))
        )
    }

    @Test
    fun `spaces are excluded`() {
        // A space is a container; it holds no messages to index.
        val summaries = listOf(
            aRoom("!space:server", timestamp = 900L, isSpace = true),
            aRoom("!room:server", timestamp = 100L),
        )

        assertThat(planSearchBackfill(summaries)).isEqualTo(listOf(RoomId("!room:server")))
    }

    @Test
    fun `tombstoned rooms are excluded`() {
        // An upgraded room's history is frozen and lives under the predecessor's id, so paginating
        // the successor spends network and returns nothing worth indexing.
        val summaries = listOf(
            aRoom("!upgraded:server", timestamp = 900L, successorRoom = SuccessorRoom(RoomId("!new:server"), null)),
            aRoom("!room:server", timestamp = 100L),
        )

        assertThat(planSearchBackfill(summaries)).isEqualTo(listOf(RoomId("!room:server")))
    }

    @Test
    fun `rooms the user has not joined are excluded`() {
        val summaries = listOf(
            aRoom("!invited:server", timestamp = 900L, membership = CurrentUserMembership.INVITED),
            aRoom("!left:server", timestamp = 800L, membership = CurrentUserMembership.LEFT),
            aRoom("!joined:server", timestamp = 100L),
        )

        assertThat(planSearchBackfill(summaries)).isEqualTo(listOf(RoomId("!joined:server")))
    }

    @Test
    fun `rooms with no latest event are excluded`() {
        // Nothing has ever arrived here, so there is no history to walk back into.
        val summaries = listOf(
            aRoom("!empty:server", latestEvent = LatestEventValue.None),
            aRoom("!room:server", timestamp = 100L),
        )

        assertThat(planSearchBackfill(summaries)).isEqualTo(listOf(RoomId("!room:server")))
    }

    @Test
    fun `low priority rooms are pushed to the tail regardless of recency`() {
        val summaries = listOf(
            aRoom("!lowButRecent:server", timestamp = 900L, isLowPriority = true),
            aRoom("!normalButOld:server", timestamp = 100L),
        )

        assertThat(planSearchBackfill(summaries)).isEqualTo(
            listOf(RoomId("!normalButOld:server"), RoomId("!lowButRecent:server"))
        )
    }

    @Test
    fun `the queue is truncated to the room limit, keeping the most recent`() {
        val summaries = (1..ROOM_QUEUE_LIMIT + 50).map { index ->
            aRoom("!room$index:server", timestamp = index.toLong())
        }

        val plan = planSearchBackfill(summaries)

        assertThat(plan).hasSize(ROOM_QUEUE_LIMIT)
        // Highest timestamp wins, so the newest room is first and the oldest 50 are dropped.
        assertThat(plan.first()).isEqualTo(RoomId("!room${ROOM_QUEUE_LIMIT + 50}:server"))
        assertThat(plan).doesNotContain(RoomId("!room1:server"))
    }

    @Test
    fun `an empty room list yields an empty plan`() {
        assertThat(planSearchBackfill(emptyList())).isEmpty()
    }
}

private fun aRoom(
    roomId: String,
    timestamp: Long = 0L,
    isSpace: Boolean = false,
    isLowPriority: Boolean = false,
    successorRoom: SuccessorRoom? = null,
    membership: CurrentUserMembership = CurrentUserMembership.JOINED,
    latestEvent: LatestEventValue = aRemoteLatestEvent(timestamp = timestamp),
): RoomSummary = aRoomSummary(
    roomId = RoomId(roomId),
    isSpace = isSpace,
    isLowPriority = isLowPriority,
    successorRoom = successorRoom,
    currentUserMembership = membership,
    latestEvent = latestEvent,
)
