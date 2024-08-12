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

package io.element.android.features.securebackup.impl.reset.root

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ResetIdentityRootPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = ResetIdentityRootPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.displayConfirmationDialog).isFalse()
        }
    }

    @Test
    fun `present - Continue event displays the confirmation dialog`() = runTest {
        val presenter = ResetIdentityRootPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ResetIdentityRootEvent.Continue)

            assertThat(awaitItem().displayConfirmationDialog).isTrue()
        }
    }

    @Test
    fun `present - DismissDialog event hides the confirmation dialog`() = runTest {
        val presenter = ResetIdentityRootPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ResetIdentityRootEvent.Continue)
            assertThat(awaitItem().displayConfirmationDialog).isTrue()

            initialState.eventSink(ResetIdentityRootEvent.DismissDialog)
            assertThat(awaitItem().displayConfirmationDialog).isFalse()
        }
    }
}
