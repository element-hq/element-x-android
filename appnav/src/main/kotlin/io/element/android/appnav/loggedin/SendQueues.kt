/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appnav.loggedin

import androidx.annotation.VisibleForTesting
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@VisibleForTesting
const val SEND_QUEUES_RETRY_DELAY_MILLIS = 500L

@SingleIn(SessionScope::class)
class SendQueues @Inject constructor(
    private val matrixClient: MatrixClient,
    private val networkMonitor: NetworkMonitor,
) {
    fun launchIn(coroutineScope: CoroutineScope) {
        networkMonitor.connectivity
            .onEach { networkStatus ->
                matrixClient.setAllSendQueuesEnabled(enabled = networkStatus == NetworkStatus.Online)
            }
            .launchIn(coroutineScope)

        @OptIn(FlowPreview::class)
        matrixClient.sendQueueDisabledFlow()
            .debounce(SEND_QUEUES_RETRY_DELAY_MILLIS)
            .onEach { _: RoomId ->
                if (networkMonitor.connectivity.value == NetworkStatus.Online) {
                    matrixClient.setAllSendQueuesEnabled(enabled = true)
                }
            }
            .launchIn(coroutineScope)
    }
}
