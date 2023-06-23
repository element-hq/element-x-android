/*
 * Copyright (c) 2023 New Vector Ltd
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

@file:OptIn(FlowPreview::class)

package io.element.android.features.networkmonitor.impl

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(scope = AppScope::class)
@SingleIn(AppScope::class)
class NetworkMonitorImpl @Inject constructor(
    @ApplicationContext context: Context,
    appCoroutineScope: CoroutineScope,
) : NetworkMonitor {

    private val connectivityManager: ConnectivityManager = context.getSystemService(ConnectivityManager::class.java)

    override val connectivity: StateFlow<NetworkStatus> = callbackFlow {

        /**
         *  Assume there is no network available as soon as onLost is called.
         *  Reading activeNetwork synchronously from the callback is not safe.
         *  If there is an other active network we'll get some callback in onCapabilitiesChanged.
         *  Debounce the result to avoid quick offline<->online changes.
         */
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                trySendBlocking(NetworkStatus.Offline)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySendBlocking(networkCapabilities.getNetworkStatus())
            }
        }
        trySendBlocking(connectivityManager.activeNetworkStatus())
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
        Timber.d("Subscribe")
        awaitClose {
            Timber.d("Unsubscribe")
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .debounce(300)
        .stateIn(appCoroutineScope, SharingStarted.WhileSubscribed(), connectivityManager.activeNetworkStatus())

    private fun ConnectivityManager.activeNetworkStatus(): NetworkStatus {
        return activeNetwork?.let {
            getNetworkCapabilities(it)?.getNetworkStatus()
        } ?: NetworkStatus.Offline
    }

    private fun NetworkCapabilities.getNetworkStatus(): NetworkStatus {
        val hasInternet = hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return if (hasInternet) {
            NetworkStatus.Online
        } else {
            NetworkStatus.Offline
        }
    }
}
