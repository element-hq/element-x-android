/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.backfill

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.roomlist.RoomSummary

/**
 * Rooms visited in one sweep generation. Bounds the work for an account with a very large room list;
 * beyond this the tail is almost always rooms the user does not read.
 */
internal const val ROOM_QUEUE_LIMIT = 200

/**
 * Decides which rooms to back-paginate, and in what order.
 *
 * Pure on purpose: this is the one part of the sweep whose behaviour a unit test can fully pin down,
 * so all the judgement lives here and the runner stays a dumb loop.
 *
 * Ordering is most-recently-active first, so the rooms a user is most likely to search become
 * searchable soonest. The sort is explicit and not inherited: the room list replays the SDK's diffs
 * verbatim and applies no app-side ordering, so relying on incoming order would be relying on an
 * accident.
 */
internal fun planSearchBackfill(summaries: List<RoomSummary>): List<RoomId> {
    return summaries
        .asSequence()
        .filter { it.info.currentUserMembership == CurrentUserMembership.JOINED }
        // Spaces are containers and hold no messages to index.
        .filter { !it.info.isSpace }
        // A tombstoned room's history is frozen and lives on under the predecessor's id, so
        // paginating the successor spends network on nothing.
        .filter { it.info.successorRoom == null }
        // No latest event means nothing has ever arrived here; there is no history to walk back into.
        .filter { it.latestEventTimestamp != null }
        .sortedWith(
            // Low priority last, then most recently active first.
            compareBy<RoomSummary> { it.info.isLowPriority }
                .thenByDescending { it.latestEventTimestamp ?: 0L }
        )
        .map { it.roomId }
        .distinct()
        .take(ROOM_QUEUE_LIMIT)
        .toList()
}
