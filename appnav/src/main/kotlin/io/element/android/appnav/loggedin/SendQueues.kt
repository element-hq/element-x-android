/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
