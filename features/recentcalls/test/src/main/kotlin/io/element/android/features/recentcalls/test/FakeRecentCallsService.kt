/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.test

import io.element.android.features.recentcalls.api.CallSessionRecorder
import io.element.android.features.recentcalls.api.RecentCallEntry
import io.element.android.features.recentcalls.api.RecentCallsFilter
import io.element.android.features.recentcalls.api.RecentCallsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeRecentCallsService(
    initialEntries: List<RecentCallEntry> = emptyList(),
    private val isLoadingFlow: Flow<Boolean> = MutableStateFlow(false),
    private val canLoadMoreFlow: Flow<Boolean> = MutableStateFlow(false),
    private val loadMoreLambda: suspend () -> Unit = {},
) : RecentCallsService {
    private val entries = MutableStateFlow(initialEntries)

    fun setEntries(value: List<RecentCallEntry>) {
        entries.value = value
    }

    override fun recentCalls(filter: RecentCallsFilter): Flow<List<RecentCallEntry>> {
        return entries.map { filter.apply(it) }
    }

    override val isLoading: Flow<Boolean> = isLoadingFlow

    override val canLoadMore: Flow<Boolean> = canLoadMoreFlow

    override suspend fun loadMore() = loadMoreLambda()
}

class FakeCallSessionRecorder(
    initialOngoing: List<RecentCallEntry> = emptyList(),
) : CallSessionRecorder {
    private val _ongoingEntries = MutableStateFlow(initialOngoing)
    override val ongoingEntries: StateFlow<List<RecentCallEntry>> = _ongoingEntries.asStateFlow()

    override fun onIncomingRing(
        roomId: io.element.android.libraries.matrix.api.core.RoomId,
        roomDisplayName: String,
        avatarUrl: String?,
        isDirect: Boolean,
        counterpartUserId: io.element.android.libraries.matrix.api.core.UserId?,
        callIntent: io.element.android.libraries.matrix.api.notification.CallIntent,
        timestamp: Long,
    ) = Unit

    override fun onJoined(roomId: io.element.android.libraries.matrix.api.core.RoomId, timestamp: Long) = Unit

    override fun onMissed(
        roomId: io.element.android.libraries.matrix.api.core.RoomId,
        callIntent: io.element.android.libraries.matrix.api.notification.CallIntent,
        timestamp: Long,
    ) = Unit

    override fun onDeclined(
        roomId: io.element.android.libraries.matrix.api.core.RoomId,
        callIntent: io.element.android.libraries.matrix.api.notification.CallIntent,
        timestamp: Long,
    ) = Unit

    override fun onCompleted(roomId: io.element.android.libraries.matrix.api.core.RoomId, durationMs: Long) = Unit
}
