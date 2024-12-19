/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(FlowPreview::class)

package io.element.android.features.networkmonitor.impl

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.networkmonitor.api.IdleModeDetector
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.user.CurrentSessionIdHolder
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ContributesBinding(scope = SessionScope::class)
@SingleIn(SessionScope::class)
class DefaultNetworkMonitor @Inject constructor(
    @ApplicationContext context: Context,
    @SessionCoroutineScope sessionCoroutineScope: CoroutineScope,
    private val sessionIdHolder: CurrentSessionIdHolder,
    private val okHttpClient: OkHttpClient,
    private val sessionStore: SessionStore,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val idleModeDetector: IdleModeDetector,
) : NetworkMonitor {
    private val tag = DefaultNetworkMonitor::class.simpleName.toString()

    private val connectivityManager: ConnectivityManager = context.getSystemService(ConnectivityManager::class.java)

    private val networkStatus: SharedFlow<NetworkStatus> = callbackFlow {
        /**
         *  Calling connectivityManager methods synchronously from the callbacks is not safe.
         *  So instead we just keep the count of active networks, ie. those checking the capability request.
         *  Debounce the result to avoid quick offline<->online changes.
         */
        val callback = object : ConnectivityManager.NetworkCallback() {
            private val activeNetworksCount = AtomicInteger(0)

            override fun onLost(network: Network) {
                if (activeNetworksCount.decrementAndGet() == 0) {
                    trySendBlocking(NetworkStatus.Offline)
                }
            }

            override fun onAvailable(network: Network) {
                if (activeNetworksCount.incrementAndGet() > 0) {
                    trySendBlocking(NetworkStatus.Online)
                }
            }
        }
        trySendBlocking(connectivityManager.activeNetworkStatus())
        val request = NetworkRequest.Builder().build()

        connectivityManager.registerNetworkCallback(request, callback)
        Timber.tag(tag).d("Subscribe")
        awaitClose {
            Timber.tag(tag).d("Unsubscribe")
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .distinctUntilChanged()
        .debounce(300)
        .onEach {
            Timber.tag(tag).d("NetworkStatus changed=$it")
        }
        .shareIn(sessionCoroutineScope, SharingStarted.WhileSubscribed(), replay = 1)

    private fun ConnectivityManager.activeNetworkStatus(): NetworkStatus {
        return if (activeNetwork != null) NetworkStatus.Online else NetworkStatus.Offline
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val connectivity: StateFlow<NetworkStatus> = networkStatus.flatMapLatest {
        if (it == NetworkStatus.Online) {
            checkHomeServerConnectivity()
        } else {
            flowOf(NetworkStatus.Offline)
        }
    }.stateIn(sessionCoroutineScope, SharingStarted.WhileSubscribed(), NetworkStatus.Online)

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun checkHomeServerConnectivity(): Flow<NetworkStatus> = withContext(coroutineDispatchers.io) {
        val homeServerUrl = sessionStore.getSession(sessionIdHolder.current.value)?.homeserverUrl?.removeSuffix("/")
        if (homeServerUrl == null) {
            return@withContext flowOf(NetworkStatus.Offline)
        }

        idleModeDetector.subscribeToIdleMode()
            .onEach { isInDoze -> Timber.tag(tag).d("Device is in doze mode=$isInDoze") }
            // Only check network connectivity while the app is in foreground,
            // otherwise Doze might prevent the network request from working
            .filter { isInDoze -> !isInDoze }
            .flatMapLatest {
                channelFlow<NetworkStatus> {
                    while (coroutineContext.isActive) {
                        val request = Request.Builder().url("$homeServerUrl/_matrix/client/versions").build()
                        try {
                            if (okHttpClient.newCall(request).execute().isSuccessful) {
                                Timber.tag(tag).d("Home server is reachable")
                                trySend(NetworkStatus.Online)
                            } else {
                                Timber.tag(tag).d("Home server is not reachable")
                                trySend(NetworkStatus.Offline)
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Timber.tag(tag).e(e, "Failed to check home server connectivity")
                            trySend(NetworkStatus.Offline)
                        }

                        delay(30.seconds)
                    }
                }
        }
            .flowOn(coroutineDispatchers.io)
    }
}
