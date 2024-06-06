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

import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

private const val MIN_RETRY_DELAY = 250L
private const val MAX_RETRY_DELAY = 5000L

@SingleIn(SessionScope::class)
class SendingQueue @Inject constructor(
    private val matrixClient: MatrixClient,
    private val networkMonitor: NetworkMonitor,
) {

    private val retryCount = AtomicInteger(0)

    fun setupWith(coroutineScope: CoroutineScope) {
        combine(
            networkMonitor.connectivity,
            matrixClient.sendingQueueStatus(),
        ) { networkStatus, isSendingQueueEnabled ->
            Pair(networkStatus, isSendingQueueEnabled)
        }.onEach { (networkStatus, isSendingQueueEnabled) ->
            Timber.d("Network status: $networkStatus, isSendingQueueEnabled: $isSendingQueueEnabled")
            if (networkStatus == NetworkStatus.Online && !isSendingQueueEnabled) {
                val retryDelay = (MIN_RETRY_DELAY * retryCount.incrementAndGet()).coerceIn(MIN_RETRY_DELAY, MAX_RETRY_DELAY)
                Timber.d("Retry enabling sending queue in $retryDelay ms")
                delay(retryDelay)
            } else {
                retryCount.set(0)
            }
            matrixClient.setSendingQueueEnabled(enabled = networkStatus == NetworkStatus.Online)
        }.launchIn(coroutineScope)
    }
}
