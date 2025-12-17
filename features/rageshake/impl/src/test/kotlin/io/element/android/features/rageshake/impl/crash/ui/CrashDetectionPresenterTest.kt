/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.crash.ui

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.rageshake.api.crash.CrashDetectionEvents
import io.element.android.features.rageshake.impl.crash.A_CRASH_DATA
import io.element.android.features.rageshake.impl.crash.DefaultCrashDetectionPresenter
import io.element.android.features.rageshake.impl.crash.FakeCrashDataStore
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CrashDetectionPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state no crash`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.crashDetected).isFalse()
        }
    }

    @Test
    fun `present - initial state crash`() = runTest {
        val presenter = createPresenter(
            FakeCrashDataStore(appHasCrashed = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.crashDetected).isTrue()
        }
    }

    @Test
    fun `present - initial state crash is ignored if the feature is not available`() = runTest {
        val presenter = createPresenter(
            FakeCrashDataStore(appHasCrashed = true),
            isFeatureAvailableFlow = flowOf(false),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.crashDetected).isFalse()
        }
    }

    @Test
    fun `present - reset app has crashed`() = runTest {
        val presenter = createPresenter(
            FakeCrashDataStore(appHasCrashed = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.crashDetected).isTrue()
            initialState.eventSink.invoke(CrashDetectionEvents.ResetAppHasCrashed)
            assertThat(awaitItem().crashDetected).isFalse()
        }
    }

    @Test
    fun `present - reset all crash data`() = runTest {
        val presenter = createPresenter(
            FakeCrashDataStore(appHasCrashed = true, crashData = A_CRASH_DATA)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.crashDetected).isTrue()
            initialState.eventSink.invoke(CrashDetectionEvents.ResetAllCrashData)
            assertThat(awaitItem().crashDetected).isFalse()
        }
    }

    @Test
    fun `present - crashDetected is false if the feature is not available`() = runTest {
        val isFeatureAvailableFlow = MutableStateFlow(false)
        val crashDataStore = FakeCrashDataStore(appHasCrashed = false)
        val presenter = createPresenter(
            crashDataStore = crashDataStore,
            isFeatureAvailableFlow = isFeatureAvailableFlow,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.crashDetected).isFalse()
            crashDataStore.setCrashData("Some crash data")
            // No new state
            crashDataStore.resetAppHasCrashed()
            // No new state
            isFeatureAvailableFlow.value = true
            crashDataStore.setCrashData("Some crash data")
            assertThat(awaitItem().crashDetected).isTrue()
            crashDataStore.resetAppHasCrashed()
            assertThat(awaitItem().crashDetected).isFalse()
            crashDataStore.setCrashData("Some crash data")
            assertThat(awaitItem().crashDetected).isTrue()
            isFeatureAvailableFlow.value = false
            assertThat(awaitItem().crashDetected).isFalse()
        }
    }

    private fun createPresenter(
        crashDataStore: FakeCrashDataStore = FakeCrashDataStore(),
        buildMeta: BuildMeta = aBuildMeta(),
        isFeatureAvailableFlow: Flow<Boolean> = flowOf(true),
    ) = DefaultCrashDetectionPresenter(
        buildMeta = buildMeta,
        crashDataStore = crashDataStore,
        rageshakeFeatureAvailability = { isFeatureAvailableFlow },
    )
}
