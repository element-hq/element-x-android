/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.impl.recentcalls

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.call.api.CallData
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.features.recentcalls.api.RecentCallsFilter
import io.element.android.features.recentcalls.api.RecentCallsService
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.notification.CallIntent
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Inject
class RecentCallsPresenter(
    private val recentCallsService: RecentCallsService,
    private val matrixClient: MatrixClient,
    private val elementCallEntryPoint: ElementCallEntryPoint,
) : Presenter<RecentCallsState> {
    @Composable
    override fun present(): RecentCallsState {
        var filter by rememberSaveable { mutableStateOf(RecentCallsFilter.ALL) }
        var isLoadingMore by rememberSaveable { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        val entries by recentCallsService.recentCalls(filter).collectAsState(initial = emptyList())
        val isLoading by recentCallsService.isLoading.collectAsState(initial = true)
        val canLoadMore by recentCallsService.canLoadMore.collectAsState(initial = false)

        fun handleEvent(event: RecentCallsEvent) {
            when (event) {
                is RecentCallsEvent.SelectFilter -> filter = event.filter
                RecentCallsEvent.LoadMore -> {
                    if (isLoadingMore || !canLoadMore) return
                    isLoadingMore = true
                    coroutineScope.launch {
                        recentCallsService.loadMore()
                        isLoadingMore = false
                    }
                }
                is RecentCallsEvent.CallBack -> {
                    elementCallEntryPoint.startCall(
                        CallData(
                            sessionId = matrixClient.sessionId,
                            roomId = event.entry.roomId,
                            isAudioCall = event.entry.callIntent == CallIntent.AUDIO,
                        )
                    )
                }
            }
        }

        return RecentCallsState(
            filter = filter,
            entries = entries.toImmutableList(),
            isLoading = isLoading,
            canLoadMore = canLoadMore,
            isLoadingMore = isLoadingMore,
            eventSink = ::handleEvent,
        )
    }
}
