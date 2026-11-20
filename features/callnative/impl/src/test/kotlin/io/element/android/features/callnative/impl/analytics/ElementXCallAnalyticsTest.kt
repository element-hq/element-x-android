/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl.analytics

import com.google.common.truth.Truth.assertThat
import io.element.android.call.api.analytics.ElementCallAnalyticsCallKind
import io.element.android.call.api.analytics.ElementCallAnalyticsEvent
import io.element.android.features.callnative.api.analytics.CallAnalyticsClient
import io.element.android.features.callnative.api.analytics.CallAnalyticsClientFactory
import io.element.android.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ElementXCallAnalyticsTest {
    private val event = ElementCallAnalyticsEvent.CallJoinAttempted(attemptId = "attempt", callKind = ElementCallAnalyticsCallKind.DM)

    @Test
    fun `without consent, nothing is sent and no client is made`() = runTest {
        val factory = FakeCallAnalyticsClientFactory()
        val analytics = createAnalytics(FakeAnalyticsService(isEnabled = false), factory)
        analytics.awaitConsent()

        analytics.track(event)

        assertThat(factory.clients).isEmpty()
    }

    @Test
    fun `with consent, each event is sent under its name with its properties unchanged`() = runTest {
        val factory = FakeCallAnalyticsClientFactory()
        val analytics = createAnalytics(FakeAnalyticsService(isEnabled = true), factory)
        analytics.awaitConsent()

        analytics.track(event)

        assertThat(factory.clients.single().captured).containsExactly("CallJoinAttempted" to event.properties)
    }

    @Test
    fun `revoking consent closes the client and drops what follows`() = runTest {
        val factory = FakeCallAnalyticsClientFactory()
        val analyticsService = FakeAnalyticsService(isEnabled = true)
        val analytics = createAnalytics(analyticsService, factory)
        analytics.awaitConsent()

        analyticsService.setUserConsent(false)
        runCurrent()
        analytics.track(event)

        val client = factory.clients.single()
        assertThat(client.isClosed).isTrue()
        assertThat(client.captured).isEmpty()
    }

    @Test
    fun `giving consent again opens a new client`() = runTest {
        val factory = FakeCallAnalyticsClientFactory()
        val analyticsService = FakeAnalyticsService(isEnabled = true)
        val analytics = createAnalytics(analyticsService, factory)
        analytics.awaitConsent()

        analyticsService.setUserConsent(false)
        runCurrent()
        analyticsService.setUserConsent(true)
        runCurrent()
        analytics.track(event)

        assertThat(factory.clients).hasSize(2)
        assertThat(factory.clients.last().captured).hasSize(1)
    }

    @Test
    fun `a build without credentials drops the events`() = runTest {
        val analytics = createAnalytics(FakeAnalyticsService(isEnabled = true)) { null }
        analytics.awaitConsent()

        // Nothing to assert on but the absence of a crash: there is no client to send to.
        analytics.track(event)
    }

    @Test
    fun `a build without a client factory, as F-Droid, drops the events`() = runTest {
        val analytics = createAnalytics(FakeAnalyticsService(isEnabled = true), factory = null)
        analytics.awaitConsent()

        // Nothing to assert on but the absence of a crash: there is no client to send to.
        analytics.track(event)
    }

    private fun TestScope.createAnalytics(
        analyticsService: FakeAnalyticsService,
        factory: CallAnalyticsClientFactory?,
    ) = ElementXCallAnalytics(
        // Its collector runs for the app's life, so not in the test's own scope, which would wait for it.
        appCoroutineScope = backgroundScope,
        analyticsService = analyticsService,
        clientFactories = setOfNotNull(factory),
    )
}

private class FakeCallAnalyticsClientFactory : CallAnalyticsClientFactory {
    val clients = mutableListOf<FakeCallAnalyticsClient>()

    override fun create(): CallAnalyticsClient = FakeCallAnalyticsClient().also { clients += it }
}

private class FakeCallAnalyticsClient : CallAnalyticsClient {
    val captured = mutableListOf<Pair<String, Map<String, Any>>>()
    var isClosed = false

    override fun capture(event: String, properties: Map<String, Any>) {
        if (!isClosed) captured += event to properties
    }

    override fun close() {
        isClosed = true
    }
}
