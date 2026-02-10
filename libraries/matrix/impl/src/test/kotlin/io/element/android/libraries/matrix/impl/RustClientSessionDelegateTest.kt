/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.impl.core.SdkBackgroundTaskError
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustSession
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import uniffi.matrix_sdk_common.BackgroundTaskFailureReason

@OptIn(ExperimentalCoroutinesApi::class)
class RustClientSessionDelegateTest {
    @Test
    fun `saveSessionInKeychain should update the store`() = runTest {
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData(
                    accessToken = "anAccessToken",
                    refreshToken = "aRefreshToken",
                )
            )
        )
        val sut = aRustClientSessionDelegate(sessionStore)
        sut.saveSessionInKeychain(
            aRustSession(
                accessToken = "at",
                refreshToken = "rt",
            )
        )
        runCurrent()
        val result = sessionStore.getLatestSession()
        assertThat(result!!.accessToken).isEqualTo("at")
        assertThat(result.refreshToken).isEqualTo("rt")
    }

    @Test
    fun `onBackgroundTaskErrorReport reports the error to analytics if recoverable`() = runTest {
        val analyticsService = FakeAnalyticsService(isEnabled = true)
        val sut = aRustClientSessionDelegate(analyticsService = analyticsService)
        sut.onBackgroundTaskErrorReport("Crasher", BackgroundTaskFailureReason.EarlyTermination)
        sut.onBackgroundTaskErrorReport("Crasher", BackgroundTaskFailureReason.Error("BOOM"))

        assertThat(analyticsService.trackedErrors).hasSize(2)
        assertThat(analyticsService.trackedErrors[0].message).isEqualTo("SDK background task 'Crasher' failure: \nEarly termination")
        assertThat(analyticsService.trackedErrors[1].message).isEqualTo("SDK background task 'Crasher' failure: \nError: BOOM")
    }

    @Test(expected = SdkBackgroundTaskError::class)
    fun `onBackgroundTaskErrorReport reports the error to analytics and throws it if it's a panic`() = runTest {
        val analyticsService = FakeAnalyticsService(isEnabled = true)
        val sut = aRustClientSessionDelegate(analyticsService = analyticsService)
        sut.onBackgroundTaskErrorReport("Crasher", BackgroundTaskFailureReason.Panic("BOOM", "Stacktrace"))

        assertThat(analyticsService.trackedErrors).hasSize(1)
        assertThat(analyticsService.trackedErrors[0].message)
            .isEqualTo("SDK background task 'Crasher' failure: \nPanic (unrecoverable): BOOM\nStacktrace")
    }
}

fun TestScope.aRustClientSessionDelegate(
    sessionStore: SessionStore = InMemorySessionStore(),
    analyticsService: AnalyticsService = FakeAnalyticsService(),
) = RustClientSessionDelegate(
    sessionStore = sessionStore,
    appCoroutineScope = this,
    analyticsService = analyticsService,
    coroutineDispatchers = testCoroutineDispatchers(),
)
