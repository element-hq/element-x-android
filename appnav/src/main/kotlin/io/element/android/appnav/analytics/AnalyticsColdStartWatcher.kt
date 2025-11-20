/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.analytics

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction.ColdStartUntilCachedRoomList
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Adds a performance check transaction measuring the time between a cold start (or, after we read the user consent after a cold start)
 * until the cached room list is displayed. This check only takes place in a cold app start after the user is authenticated.
 */
interface AnalyticsColdStartWatcher {
    fun start()
    fun whenLoggingIn()
    fun onRoomListVisible()
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultAnalyticsColdStartWatcher(
    private val analyticsService: AnalyticsService,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
) : AnalyticsColdStartWatcher {
    private val isColdStart = AtomicBoolean(true)

    override fun start() {
        analyticsService.userConsentFlow
            .onEach { hasConsent ->
                if (hasConsent) {
                    if (isColdStart.get()) {
                        Timber.d("Starting cold start check")
                        analyticsService.startLongRunningTransaction(ColdStartUntilCachedRoomList)
                    } else {
                        error("The app is no longer in a cold start state")
                    }
                }
            }
            .catch { Timber.w(it.message) }
            .launchIn(appCoroutineScope)
    }

    override fun whenLoggingIn() {
        if (isColdStart.getAndSet(false)) {
            analyticsService.removeLongRunningTransaction(ColdStartUntilCachedRoomList)
            Timber.d("Canceled cold start check: user is logging in")
        }
    }

    override fun onRoomListVisible() {
        if (isColdStart.getAndSet(false)) {
            Timber.d("Room list is visible, finishing cold start check")
            analyticsService.removeLongRunningTransaction(ColdStartUntilCachedRoomList)?.finish()
        }
    }
}
