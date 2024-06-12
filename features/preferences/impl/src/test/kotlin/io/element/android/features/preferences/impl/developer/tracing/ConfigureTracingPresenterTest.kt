/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
