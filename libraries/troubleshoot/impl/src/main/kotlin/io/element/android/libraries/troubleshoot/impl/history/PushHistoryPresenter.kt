/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.push.api.PushService
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

fun interface PushHistoryNavigator {
    fun navigateTo(roomId: RoomId, eventId: EventId)
}

@AssistedInject
class PushHistoryPresenter(
    @Assisted private val pushHistoryNavigator: PushHistoryNavigator,
    private val pushService: PushService,
    matrixClient: MatrixClient,
) : Presenter<PushHistoryState> {
    @AssistedFactory
    fun interface Factory {
        fun create(pushHistoryNavigator: PushHistoryNavigator): PushHistoryPresenter
    }

    private val sessionId = matrixClient.sessionId

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
        var showNotSameAccountError by remember { mutableStateOf(false) }

        fun handleEvent(event: PushHistoryEvents) {
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
                    showNotSameAccountError = false
                }
                is PushHistoryEvents.NavigateTo -> {
                    if (event.sessionId != sessionId) {
                        showNotSameAccountError = true
                    } else {
                        pushHistoryNavigator.navigateTo(event.roomId, event.eventId)
                    }
                }
            }
        }

        return PushHistoryState(
            pushCounter = pushCounter,
            pushHistoryItems = pushHistory.toImmutableList(),
            showOnlyErrors = showOnlyErrors,
            resetAction = resetAction,
            showNotSameAccountError = showNotSameAccountError,
            eventSink = ::handleEvent,
        )
    }
}
