/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.impl.recentcalls

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.recentcalls.api.RecentCallDirection
import io.element.android.features.recentcalls.api.RecentCallEntry
import io.element.android.features.recentcalls.api.RecentCallStatus
import io.element.android.features.recentcalls.api.RecentCallsFilter
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.CallIntent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class RecentCallsStateProvider : PreviewParameterProvider<RecentCallsState> {
    override val values: Sequence<RecentCallsState>
        get() = sequenceOf(
            aRecentCallsState(),
            aRecentCallsState(isLoading = true, entries = persistentListOf()),
            aRecentCallsState(filter = RecentCallsFilter.MISSED),
        )
}

fun aRecentCallsState(
    filter: RecentCallsFilter = RecentCallsFilter.ALL,
    entries: ImmutableList<RecentCallEntry> = aRecentCallEntries(),
    isLoading: Boolean = false,
    canLoadMore: Boolean = true,
    isLoadingMore: Boolean = false,
    eventSink: (RecentCallsEvent) -> Unit = {},
) = RecentCallsState(
    filter = filter,
    entries = entries,
    isLoading = isLoading,
    canLoadMore = canLoadMore,
    isLoadingMore = isLoadingMore,
    eventSink = eventSink,
)

private fun aRecentCallEntries() = listOf(
    RecentCallEntry(
        id = "1",
        eventId = EventId("\$1"),
        roomId = RoomId("!room:example.com"),
        roomDisplayName = "Alice",
        avatarUrl = null,
        isDirect = true,
        counterpartUserId = UserId("@alice:example.com"),
        direction = RecentCallDirection.INCOMING,
        status = RecentCallStatus.MISSED,
        callIntent = CallIntent.VIDEO,
        timestamp = 1_700_000_000_000,
        durationMs = null,
    ),
    RecentCallEntry(
        id = "2",
        eventId = EventId("\$2"),
        roomId = RoomId("!room2:example.com"),
        roomDisplayName = "Team chat",
        avatarUrl = null,
        isDirect = false,
        counterpartUserId = null,
        direction = RecentCallDirection.OUTGOING,
        status = RecentCallStatus.COMPLETED,
        callIntent = CallIntent.AUDIO,
        timestamp = 1_699_000_000_000,
        durationMs = 120_000,
    ),
).toImmutableList()
