/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.analytics.impl.preferences

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.analytics.api.AnalyticsOptInEvents
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AnalyticsPreferencesPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state available`() = runTest {
        val presenter = AnalyticsPreferencesPresenter(
            FakeAnalyticsService(isEnabled = true, didAskUserConsent = true),
            aBuildMeta()
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isEnabled).isTrue()
            assertThat(initialState.policyUrl).isNotEmpty()
        }
    }

    @Test
    fun `present - initial state not available`() = runTest {
        val presenter = AnalyticsPreferencesPresenter(
            FakeAnalyticsService(isEnabled = false, didAskUserConsent = false),
            aBuildMeta()
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isEnabled).isFalse()
        }
    }

    @Test
    fun `present - enable and disable`() = runTest {
        val presenter = AnalyticsPreferencesPresenter(
            FakeAnalyticsService(isEnabled = true, didAskUserConsent = true),
            aBuildMeta()
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isEnabled).isTrue()
            initialState.eventSink.invoke(AnalyticsOptInEvents.EnableAnalytics(false))
            assertThat(awaitItem().isEnabled).isFalse()
            initialState.eventSink.invoke(AnalyticsOptInEvents.EnableAnalytics(true))
            assertThat(awaitItem().isEnabled).isTrue()
        }
    }
}
