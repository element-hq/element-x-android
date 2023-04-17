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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(scope = AppScope::class)
@SingleIn(AppScope::class)
class NetworkMonitorImpl @Inject constructor(
    @ApplicationContext context: Context
) : NetworkMonitor {

    private val connectivityManager: ConnectivityManager = context.getSystemService(ConnectivityManager::class.java)

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _connectivity.value = connectivityManager.currentConnectionStatus()
            Timber.v("Connectivity status (available): ${connectivityManager.currentConnectionStatus()}")
        }

        override fun onLost(network: Network) {
            _connectivity.value = connectivityManager.currentConnectionStatus()
            Timber.v("Connectivity status (lost): ${connectivityManager.currentConnectionStatus()}")
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            _connectivity.value = connectivityManager.currentConnectionStatus()
            Timber.v("Connectivity status (changed): ${connectivityManager.currentConnectionStatus()}")
        }
    }

    private val _connectivity = MutableStateFlow(NetworkStatus.Online)
    override val connectivity: Flow<NetworkStatus> = _connectivity

    override val currentConnectivityStatus: NetworkStatus get() = _connectivity.value

    init {
        listenToConnectionChanges()
    }

    private fun listenToConnectionChanges() {
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        _connectivity.tryEmit(connectivityManager.currentConnectionStatus())
    }

    private fun ConnectivityManager.currentConnectionStatus(): NetworkStatus {
        val hasInternet = activeNetwork?.let(::getNetworkCapabilities)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            ?: false
        return if (hasInternet) {
            NetworkStatus.Online
        } else {
            NetworkStatus.Offline
        }
    }
}
