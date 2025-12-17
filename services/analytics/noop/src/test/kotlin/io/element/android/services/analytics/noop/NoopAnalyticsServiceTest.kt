/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.noop

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.CallStarted
import im.vector.app.features.analytics.plan.MobileScreen
import im.vector.app.features.analytics.plan.SuperProperties
import im.vector.app.features.analytics.plan.UserProperties
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NoopAnalyticsServiceTest {
    @Test
    fun `getAvailableAnalyticsProviders returns emptySet`() {
        val sut = NoopAnalyticsService()
        assertThat(sut.getAvailableAnalyticsProviders()).isEmpty()
    }

    @Test
    fun `didAskUserConsentFlow emits only true`() = runTest {
        val sut = NoopAnalyticsService()
        sut.didAskUserConsentFlow.test {
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `analyticsIdFlow emits only empty string`() = runTest {
        val sut = NoopAnalyticsService()
        sut.analyticsIdFlow.test {
            assertThat(awaitItem()).isEmpty()
            sut.setAnalyticsId("anId")
            awaitComplete()
        }
    }

    @Test
    fun `userConsentFlow emits only false`() = runTest {
        val sut = NoopAnalyticsService()
        sut.userConsentFlow.test {
            assertThat(awaitItem()).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `test no op methods`() = runTest {
        val sut = NoopAnalyticsService()
        sut.setUserConsent(false)
        sut.setUserConsent(true)
        sut.setDidAskUserConsent()
        sut.setAnalyticsId("anId")
        sut.capture(CallStarted(true, 1, true))
        sut.screen(MobileScreen(screenName = MobileScreen.ScreenName.RoomMembers))
        sut.updateUserProperties(UserProperties())
        sut.trackError(Exception("an_error"))
        sut.updateSuperProperties(SuperProperties())
    }
}
