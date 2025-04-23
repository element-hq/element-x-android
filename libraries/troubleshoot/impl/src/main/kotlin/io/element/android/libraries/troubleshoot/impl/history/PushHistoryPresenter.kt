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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.push.api.PushService
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class PushHistoryPresenter @Inject constructor(
    private val pushService: PushService,
) : Presenter<PushHistoryState> {
    @Composable
    override fun present(): PushHistoryState {
        val coroutineScope = rememberCoroutineScope()
        val pushCounter by pushService.pushCounter.collectAsState(0)
        var showOnlyErrors: Boolean by remember { mutableStateOf(false) }
        val pushHistory by remember(showOnlyErrors) {
            pushService.getPushHistoryItemsFlow().map {
                if (showOnlyErrors) {
                    it.filter { item -> item.hasBeenResolved.not() }
                } else {
                    it
                }
            }
        }.collectAsState(emptyList())
        var resetAction: AsyncAction<Unit> by remember { mutableStateOf(AsyncAction.Uninitialized) }

        fun handleEvents(event: PushHistoryEvents) {
            when (event) {
                is PushHistoryEvents.SetShowOnlyErrors -> {
                    showOnlyErrors = event.showOnlyErrors
                }
                is PushHistoryEvents.Reset -> {
                    if (event.requiresConfirmation) {
                        resetAction = AsyncAction.ConfirmingNoParams
                    } else {
                        resetAction = AsyncAction.Loading
                        coroutineScope.launch {
                            pushService.resetPushHistory()
                            resetAction = AsyncAction.Uninitialized
                        }
                    }
                }
                PushHistoryEvents.ClearDialog -> {
                    resetAction = AsyncAction.Uninitialized
                }
            }
        }

        return PushHistoryState(
            pushCounter = pushCounter,
            pushHistoryItems = pushHistory.toImmutableList(),
            showOnlyErrors = showOnlyErrors,
            resetAction = resetAction,
            eventSink = ::handleEvents
        )
    }
}
