/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.preferences

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesEvents
import io.element.android.features.rageshake.impl.rageshake.A_SENSITIVITY
import io.element.android.features.rageshake.impl.rageshake.FakeRageShake
import io.element.android.features.rageshake.impl.rageshake.FakeRageshakeDataStore
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RageshakePreferencesPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state available`() = runTest {
        val presenter = DefaultRageshakePreferencesPresenter(
            FakeRageShake(isAvailableValue = true),
            FakeRageshakeDataStore(isEnabled = true),
            rageshakeFeatureAvailability = { flowOf(true) },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isSupported).isTrue()
            assertThat(initialState.isEnabled).isTrue()
        }
    }

    @Test
    fun `present - initial state not available`() = runTest {
        val presenter = DefaultRageshakePreferencesPresenter(
            FakeRageShake(isAvailableValue = false),
            FakeRageshakeDataStore(isEnabled = true),
            rageshakeFeatureAvailability = { flowOf(true) },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isSupported).isFalse()
            assertThat(initialState.isEnabled).isTrue()
        }
    }

    @Test
    fun `present - enable and disable`() = runTest {
        val presenter = DefaultRageshakePreferencesPresenter(
            FakeRageShake(isAvailableValue = true),
            FakeRageshakeDataStore(isEnabled = true),
            rageshakeFeatureAvailability = { flowOf(true) },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isEnabled).isTrue()
            initialState.eventSink.invoke(RageshakePreferencesEvents.SetIsEnabled(false))
            assertThat(awaitItem().isEnabled).isFalse()
            initialState.eventSink.invoke(RageshakePreferencesEvents.SetIsEnabled(true))
            assertThat(awaitItem().isEnabled).isTrue()
        }
    }

    @Test
    fun `present - set sensitivity`() = runTest {
        val presenter = DefaultRageshakePreferencesPresenter(
            FakeRageShake(isAvailableValue = true),
            FakeRageshakeDataStore(isEnabled = true),
            rageshakeFeatureAvailability = { flowOf(true) },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.sensitivity).isEqualTo(A_SENSITIVITY)
            initialState.eventSink.invoke(RageshakePreferencesEvents.SetSensitivity(A_SENSITIVITY + 1f))
            assertThat(awaitItem().sensitivity).isEqualTo(A_SENSITIVITY + 1f)
        }
    }
}
