/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.analytics.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.analytics.api.AnalyticsOptInEvents
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AnalyticsOptInPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - enable`() = runTest {
        val analyticsService = FakeAnalyticsService(isEnabled = false)
        val presenter = AnalyticsOptInPresenter(
            aBuildMeta(),
            analyticsService
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(analyticsService.didAskUserConsentFlow.first()).isFalse()
            initialState.eventSink.invoke(AnalyticsOptInEvents.EnableAnalytics(true))
            assertThat(analyticsService.didAskUserConsentFlow.first()).isTrue()
            assertThat(analyticsService.userConsentFlow.first()).isTrue()
        }
    }

    @Test
    fun `present - not now`() = runTest {
        val analyticsService = FakeAnalyticsService(isEnabled = false)
        val presenter = AnalyticsOptInPresenter(
            aBuildMeta(),
            analyticsService
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(analyticsService.didAskUserConsentFlow.first()).isFalse()
            initialState.eventSink.invoke(AnalyticsOptInEvents.EnableAnalytics(false))
            assertThat(analyticsService.didAskUserConsentFlow.first()).isTrue()
            assertThat(analyticsService.userConsentFlow.first()).isFalse()
        }
    }
}
