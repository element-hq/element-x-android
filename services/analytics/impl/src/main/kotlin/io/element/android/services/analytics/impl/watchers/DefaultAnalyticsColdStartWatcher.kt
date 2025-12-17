/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.impl.watchers

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.api.cancelLongRunningTransaction
import io.element.android.services.analytics.api.finishLongRunningTransaction
import io.element.android.services.analytics.api.watchers.AnalyticsColdStartWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

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
                        analyticsService.startLongRunningTransaction(AnalyticsLongRunningTransaction.ColdStartUntilCachedRoomList)
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
            analyticsService.cancelLongRunningTransaction(AnalyticsLongRunningTransaction.ColdStartUntilCachedRoomList)
            Timber.d("Canceled cold start check: user is logging in")
        }
    }

    override fun onRoomListVisible() {
        if (isColdStart.getAndSet(false)) {
            Timber.d("Room list is visible, finishing cold start check")
            analyticsService.finishLongRunningTransaction(AnalyticsLongRunningTransaction.ColdStartUntilCachedRoomList)
        }
    }
}
