/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.push.api.PushService
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

class PushHistoryPresenter @Inject constructor(
    private val pushService: PushService,
) : Presenter<PushHistoryState> {
    @Composable
    override fun present(): PushHistoryState {
        val coroutineScope = rememberCoroutineScope()
        val pushCounter by pushService.pushCounter.collectAsState(0)
        val pushHistory by remember {
            pushService.getPushHistoryItemsFlow()
        }.collectAsState(emptyList())

        fun handleEvents(event: PushHistoryEvents) {
            when (event) {
                PushHistoryEvents.Reset -> coroutineScope.launch {
                    pushService.resetPushHistory()
                }
            }
        }

        return PushHistoryState(
            pushCounter = pushCounter,
            pushHistoryItems = pushHistory.toImmutableList(),
            eventSink = ::handleEvents
        )
    }
}
