/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    private fun createPresenter(
        provideResult: () -> List<DependencyLicenseItem>
    ) = DependencyLicensesListPresenter(
        licensesProvider = FakeLicensesProvider(provideResult),
    )
}
