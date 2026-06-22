/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(FlowPreview::class)

package io.element.android.features.networkmonitor.impl

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

@ContributesBinding(scope = AppScope::class)
@SingleIn(AppScope::class)
class DefaultNetworkMonitor(
    @ApplicationContext context: Context,
    @AppCoroutineScope appCoroutineScope: CoroutineScope,
) : NetworkMonitor {
    private val connectivityManager: ConnectivityManager = context.getSystemService(ConnectivityManager::class.java)

    override val isNetworkBlocked = MutableStateFlow(NetworkBlockedChecker(connectivityManager).isNetworkBlocked())
    override val isInAirGappedEnvironment = MutableStateFlow(false)

    override val connectivity: StateFlow<NetworkStatus> = callbackFlow {

        /**
         *  Calling connectivityManager methods synchronously from the callbacks is not safe.
         *  So instead we just keep the count of active networks, ie. those checking the capability request.
         *  Debounce the result to avoid quick offline<->online changes.
         */
        val callback = object : ConnectivityManager.NetworkCallback() {
            private val activeNetworksCount = AtomicInteger(0)

            override fun onLost(network: Network) {
                if (activeNetworksCount.decrementAndGet() == 0) {
                    trySendBlocking(NetworkStatus.Disconnected)
                }
            }

            override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
                Timber.d("Network ${network.networkHandle} blocked status changed: $blocked.")
                if (network.networkHandle == connectivityManager.activeNetwork?.networkHandle) {
                    // If the network is blocked, it means that Doze is preventing the app from using the network, even if it's available.
                    isNetworkBlocked.value = blocked
                }
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                if (network.networkHandle == connectivityManager.activeNetwork?.networkHandle) {
                    // If the network doesn't have the NET_CAPABILITY_VALIDATED capability, it means that the network is not able to reach the internet
                    // (according to Google), which is a common case in air-gapped environments.
                    isInAirGappedEnvironment.value = !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                }
            }

            override fun onAvailable(network: Network) {
                if (activeNetworksCount.incrementAndGet() > 0) {
                    trySendBlocking(NetworkStatus.Connected)
                }
            }
        }
        trySendBlocking(connectivityManager.activeNetworkStatus())
        val request = NetworkRequest.Builder().build()

        connectivityManager.registerNetworkCallback(request, callback)
        Timber.d("Subscribe")
        awaitClose {
            Timber.d("Unsubscribe")
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .distinctUntilChanged()
        .debounce(300)
        .onEach {
            Timber.d("NetworkStatus changed=$it")
        }
        .stateIn(appCoroutineScope, SharingStarted.WhileSubscribed(), connectivityManager.activeNetworkStatus())

    private fun ConnectivityManager.activeNetworkStatus(): NetworkStatus {
        return if (activeNetwork != null) NetworkStatus.Connected else NetworkStatus.Disconnected
    }
}
