/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl.analytics

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.call.api.analytics.ElementCallAnalytics
import io.element.android.call.api.analytics.ElementCallAnalyticsEvent
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Forwards the native call's analytics to the PostHog project the embedded (WebView) Element Call reports to, so both
 * call paths land in the same graphs. Each event's name and properties go unchanged, as the component asks.
 *
 * Only with the user's consent to analytics, which is also when the embedded call is handed its PostHog credentials
 * (`DefaultCallWidgetSettingsProvider`). Revoking consent closes the client, after flushing what was recorded before.
 */
@SingleIn(AppScope::class)
@Inject
class ElementXCallAnalytics(
    @AppCoroutineScope appCoroutineScope: CoroutineScope,
    analyticsService: AnalyticsService,
    private val clientFactory: CallAnalyticsClientFactory,
) : ElementCallAnalytics {
    @Volatile
    private var client: CallAnalyticsClient? = null

    private val consentKnown = CompletableDeferred<Unit>()

    init {
        appCoroutineScope.launch {
            analyticsService.userConsentFlow.distinctUntilChanged().collect { hasConsent ->
                if (hasConsent) open() else close()
                consentKnown.complete(Unit)
            }
        }
    }

    /**
     * Suspends until the user's consent has been read once. Called before a call stack is built, so that the first
     * attempt's events are not dropped while the preference is still loading.
     */
    suspend fun awaitConsent() = consentKnown.await()

    override fun track(event: ElementCallAnalyticsEvent) {
        client?.capture(event.name, event.properties)
    }

    private fun open() {
        if (client != null) return
        client = clientFactory.create()
        if (client == null) Timber.d("NativeCall: no PostHog credentials for the call, its analytics are dropped")
    }

    private fun close() {
        client?.close()
        client = null
    }
}
