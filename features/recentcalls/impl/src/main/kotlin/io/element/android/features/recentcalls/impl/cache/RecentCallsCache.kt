/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.impl.cache

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.recentcalls.api.CallSessionRecorder
import io.element.android.features.recentcalls.api.RecentCallEntry
import io.element.android.features.recentcalls.api.RecentCallsFilter
import io.element.android.libraries.di.SessionScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@SingleIn(SessionScope::class)
@Inject
class RecentCallsCache(
    private val callSessionRecorder: CallSessionRecorder,
) {
    private val mutex = Mutex()
    private val timelineEntries = MutableStateFlow<List<RecentCallEntry>>(emptyList())

    suspend fun setTimelineEntries(entries: List<RecentCallEntry>) = mutex.withLock {
        timelineEntries.value = entries
    }

    fun recentCalls(filter: RecentCallsFilter): Flow<List<RecentCallEntry>> {
        return combine(
            timelineEntries,
            callSessionRecorder.ongoingEntries,
        ) { timeline, ongoing ->
            mergeEntries(timeline, ongoing)
        }.map { filter.apply(it) }
    }

    private fun mergeEntries(
        timeline: List<RecentCallEntry>,
        ongoing: List<RecentCallEntry>,
    ): List<RecentCallEntry> {
        if (ongoing.isEmpty()) return timeline
        val ongoingRoomIds = ongoing.map { it.roomId }.toSet()
        val withoutOngoingRooms = timeline.filterNot { it.roomId in ongoingRoomIds }
        return (ongoing + withoutOngoingRooms).sortedByDescending { it.timestamp }
    }
}
