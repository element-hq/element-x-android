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

package io.element.android.features.location.impl.show

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.features.location.api.Location
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ShowLocationPresenterTest {

    private val actions = FakeLocationActions()
    private val location = Location(1.23, 4.56, 7.8f)

    @Test
    fun `emits initial state`() = runTest {
        val presenter = ShowLocationPresenter(
            actions,
            location,
            A_DESCRIPTION,
        )

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.location).isEqualTo(location)
            Truth.assertThat(initialState.description).isEqualTo(A_DESCRIPTION)
        }
    }

    @Test
    fun `uses action to share location`() = runTest {
        val presenter = ShowLocationPresenter(
            actions,
            location,
            A_DESCRIPTION,
        )

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ShowLocationEvents.Share)

            Truth.assertThat(actions.sharedLocation).isEqualTo(location)
            Truth.assertThat(actions.sharedLabel).isEqualTo(A_DESCRIPTION)
        }
    }

    companion object {
        private const val A_DESCRIPTION = "My happy place"
    }

}
