/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.impl.watchers

import com.google.common.truth.Truth.assertThat
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction.ColdStartUntilCachedRoomList
import io.element.android.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAnalyticsColdStartWatcherTest {
    @Test
    fun `watch - until room list is visible`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val watcher = createAnalyticsColdStartWatcher(analyticsService)

        // Start watching
        watcher.start()

        // The user has given analytics consent, we can start tracking the cold start
        analyticsService.setUserConsent(true)
        runCurrent()

        // The transaction is running
        assertThat(analyticsService.getLongRunningTransaction(ColdStartUntilCachedRoomList)).isNotNull()

        // As soon as the room list is visible
        watcher.onRoomListVisible()
        runCurrent()

        // The transaction is now finished
        assertThat(analyticsService.getLongRunningTransaction(ColdStartUntilCachedRoomList)).isNull()
    }

    @Test
    fun `watch - user is logging in, transaction is cancelled since it's not a cold start`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val watcher = createAnalyticsColdStartWatcher(analyticsService)

        // Start watching
        watcher.start()

        // The user has given analytics consent, we can start tracking the cold start
        analyticsService.setUserConsent(true)
        runCurrent()

        // The transaction is running
        assertThat(analyticsService.getLongRunningTransaction(ColdStartUntilCachedRoomList)).isNotNull()

        // If the user starts a login flow
        watcher.whenLoggingIn()
        runCurrent()

        // The transaction is gone
        assertThat(analyticsService.getLongRunningTransaction(ColdStartUntilCachedRoomList)).isNull()
    }

    @Test
    fun `watch - user was logging in, transaction is never started since it's not a cold start`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val watcher = createAnalyticsColdStartWatcher(analyticsService)

        // Start watching
        watcher.start()

        // If the user starts a login flow
        watcher.whenLoggingIn()

        // The user has given analytics consent, we can start tracking the cold start
        analyticsService.setUserConsent(true)
        runCurrent()

        // The transaction never starts
        assertThat(analyticsService.getLongRunningTransaction(ColdStartUntilCachedRoomList)).isNull()
    }

    @Test
    fun `watch - never gets consent so it does nothing`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val watcher = createAnalyticsColdStartWatcher(analyticsService)

        // Start watching
        watcher.start()

        // The user never gets the analytics consent, so we do nothing
        runCurrent()

        // The transaction is not running in that case
        assertThat(analyticsService.getLongRunningTransaction(ColdStartUntilCachedRoomList)).isNull()
    }

    private fun TestScope.createAnalyticsColdStartWatcher(
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
    ) = DefaultAnalyticsColdStartWatcher(
        analyticsService = analyticsService,
        appCoroutineScope = backgroundScope,
    )
}
