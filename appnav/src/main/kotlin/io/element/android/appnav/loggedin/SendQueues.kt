/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.loggedin

import androidx.annotation.VisibleForTesting
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.sync.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

@VisibleForTesting
const val SEND_QUEUES_RETRY_DELAY_MILLIS = 500L

@SingleIn(SessionScope::class)
@Inject
class SendQueues(
    private val matrixClient: MatrixClient,
    private val syncService: SyncService,
) {
    /**
     * Launches the send queues retry mechanism in the given [coroutineScope].
     * It runs for the lifetime of that scope. Every send queue is re-enabled [SEND_QUEUES_RETRY_DELAY_MILLIS] ms after the sync
     * state reaches [SyncState.Running] (including a sync that is already running when this is called) and after any send queue
     * reports an error while the sync is running.
     */
    @OptIn(FlowPreview::class)
    fun launchIn(coroutineScope: CoroutineScope) {
        combine(
            syncService.syncState,
            matrixClient.sendQueueDisabledFlow()
                .onEach { Timber.tag("SendQueues").d("Send queue disabled for room: $it") }
                .map { }
                // `combine` stays silent until every flow it receives has emitted at least once. A send queue may already have been
                // disabled before collection began, so emit an initial value rather than waiting for an error.
                .onStart { emit(Unit) },
        ) { syncState, _ -> syncState }
            .debounce(SEND_QUEUES_RETRY_DELAY_MILLIS)
            .onEach { syncState ->
                Timber.tag("SendQueues").d("Sync state changed: $syncState")
                if (syncState == SyncState.Running) {
                    Timber.tag("SendQueues").d("Enabling send queues again")
                    matrixClient.setAllSendQueuesEnabled(enabled = true)
                }
            }
            .launchIn(coroutineScope)
    }
}
