/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.tracing

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.matrix.api.tracing.Target
import io.element.android.libraries.matrix.api.tracing.TracingFilterConfigurations
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.waitForPredicate
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ConfigureTracingPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val store = InMemoryTracingConfigurationStore()
        val presenter = ConfigureTracingPresenter(
            store,
            TargetLogLevelMapBuilder(store, TracingFilterConfigurations.debug),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.targetsToLogLevel).isNotEmpty()
            assertThat(initialState.targetsToLogLevel[Target.MATRIX_SDK_CRYPTO]).isEqualTo(LogLevel.DEBUG)
        }
    }

    @Test
    fun `present - store is taken into account`() = runTest {
        val store = InMemoryTracingConfigurationStore()
        store.givenLogLevel(LogLevel.ERROR)
        val presenter = ConfigureTracingPresenter(
            store,
            TargetLogLevelMapBuilder(store, TracingFilterConfigurations.debug),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.targetsToLogLevel).isNotEmpty()
            assertThat(initialState.targetsToLogLevel[Target.MATRIX_SDK_CRYPTO]).isEqualTo(LogLevel.ERROR)
        }
    }

    @Test
    fun `present - change a value`() = runTest {
        val store = InMemoryTracingConfigurationStore()
        val presenter = ConfigureTracingPresenter(
            store,
            TargetLogLevelMapBuilder(store, TracingFilterConfigurations.debug),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.targetsToLogLevel[Target.MATRIX_SDK_CRYPTO]).isEqualTo(LogLevel.DEBUG)
            initialState.eventSink.invoke(ConfigureTracingEvents.UpdateFilter(Target.MATRIX_SDK_CRYPTO, LogLevel.WARN))
            val finalState = awaitItem()
            assertThat(finalState.targetsToLogLevel[Target.MATRIX_SDK_CRYPTO]).isEqualTo(LogLevel.WARN)
            waitForPredicate { store.hasStoreLogLevelBeenCalled }
        }
    }

    @Test
    fun `present - reset`() = runTest {
        val store = InMemoryTracingConfigurationStore()
        val presenter = ConfigureTracingPresenter(
            store,
            TargetLogLevelMapBuilder(store, TracingFilterConfigurations.debug),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.targetsToLogLevel[Target.MATRIX_SDK_CRYPTO]).isEqualTo(LogLevel.DEBUG)
            initialState.eventSink.invoke(ConfigureTracingEvents.UpdateFilter(Target.MATRIX_SDK_CRYPTO, LogLevel.WARN))
            val finalState = awaitItem()
            assertThat(finalState.targetsToLogLevel[Target.MATRIX_SDK_CRYPTO]).isEqualTo(LogLevel.WARN)
            waitForPredicate { store.hasStoreLogLevelBeenCalled }
            finalState.eventSink.invoke(ConfigureTracingEvents.ResetFilters)
            val resetState = awaitItem()
            assertThat(resetState.targetsToLogLevel[Target.MATRIX_SDK_CRYPTO]).isEqualTo(LogLevel.DEBUG)
            waitForPredicate { store.hasResetBeenCalled }
        }
    }
}
