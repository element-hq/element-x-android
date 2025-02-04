/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.loggedin

import androidx.annotation.VisibleForTesting
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.sync.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@VisibleForTesting
const val SEND_QUEUES_RETRY_DELAY_MILLIS = 500L

@SingleIn(SessionScope::class)
class SendQueues @Inject constructor(
    private val matrixClient: MatrixClient,
    private val syncService: SyncService,
) {
    /**
     * Launches the send queues retry mechanism in the given [coroutineScope].
     * Makes sure to re-enable all send queues when the network status is [NetworkStatus.Online].
     */
    @OptIn(FlowPreview::class)
    fun launchIn(coroutineScope: CoroutineScope) {
        combine(
            syncService.syncState,
            matrixClient.sendQueueDisabledFlow(),
        ) { syncState, _ -> syncState }
            .debounce(SEND_QUEUES_RETRY_DELAY_MILLIS)
            .onEach { syncState ->
                if (syncState == SyncState.Running) {
                    matrixClient.setAllSendQueuesEnabled(enabled = true)
                }
            }
            .launchIn(coroutineScope)
    }
}
