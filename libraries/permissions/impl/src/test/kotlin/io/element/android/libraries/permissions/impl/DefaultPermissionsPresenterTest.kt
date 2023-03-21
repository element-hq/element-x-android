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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.permissions.impl

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

const val A_PERMISSION = "A_PERMISSION"

class DefaultPermissionsPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = DefaultPermissionsPresenter(
            InMemoryPermissionsStore()
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.setParameter(A_PERMISSION)
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.showDialog).isTrue()
        }
    }
}
