/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.licenses.impl.list

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.licenses.impl.model.DependencyLicenseItem
import io.element.android.libraries.architecture.AsyncData
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DependencyLicensesListPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state, no licenses`() = runTest {
        val presenter = createPresenter { emptyList() }
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.licenses).isInstanceOf(AsyncData.Loading::class.java)
            val finalState = awaitItem()
            assertThat(finalState.licenses.isSuccess()).isTrue()
            assertThat(finalState.licenses.dataOrNull()).isEmpty()
            assertThat(finalState.filter).isEqualTo("")
        }
    }

    @Test
    fun `present - initial state, one license`() = runTest {
        val anItem = aDependencyLicenseItem()
        val presenter = createPresenter {
            listOf(anItem)
        }
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.licenses).isInstanceOf(AsyncData.Loading::class.java)
            val finalState = awaitItem()
            assertThat(finalState.licenses.isSuccess()).isTrue()
            assertThat(finalState.licenses.dataOrNull()!!.size).isEqualTo(1)
            assertThat(finalState.licenses.dataOrNull()!!.get(0)).isEqualTo(anItem)
        }
    }

    @Test
    fun `present - initial state, one license, set filter`() = runTest {
        val anItem = aDependencyLicenseItem()
        val presenter = createPresenter {
            listOf(anItem)
        }
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.licenses).isInstanceOf(AsyncData.Loading::class.java)
            val loadedState = awaitItem()
            assertThat(loadedState.licenses.isSuccess()).isTrue()
            assertThat(loadedState.licenses.dataOrNull()!!.size).isEqualTo(1)
            loadedState.eventSink(DependencyLicensesListEvent.SetFilter("dep"))
            awaitItem().let { state ->
                assertThat(state.licenses.dataOrNull()!!.size).isEqualTo(1)
                assertThat(state.filter).isEqualTo("dep")
            }
            loadedState.eventSink(DependencyLicensesListEvent.SetFilter("bleh"))
            skipItems(1)
            awaitItem().let { state ->
                assertThat(state.licenses.dataOrNull()!!.size).isEqualTo(0)
                assertThat(state.filter).isEqualTo("bleh")
            }
            loadedState.eventSink(DependencyLicensesListEvent.SetFilter(""))
            skipItems(1)
            awaitItem().let { state ->
                assertThat(state.licenses.dataOrNull()!!.size).isEqualTo(1)
                assertThat(state.filter).isEqualTo("")
            }
        }
    }

    private fun createPresenter(
        provideResult: () -> List<DependencyLicenseItem>
    ) = DependencyLicensesListPresenter(
        licensesProvider = FakeLicensesProvider(provideResult),
    )
}
