/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.analytics

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.analytics.impl.preferences.DefaultAnalyticsPreferencesPresenter
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AnalyticsSettingsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val analyticsPresenter = DefaultAnalyticsPreferencesPresenter(FakeAnalyticsService(), aBuildMeta())
        val presenter = AnalyticsSettingsPresenter(
            analyticsPresenter,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.analyticsState.isEnabled).isFalse()
        }
    }
}
