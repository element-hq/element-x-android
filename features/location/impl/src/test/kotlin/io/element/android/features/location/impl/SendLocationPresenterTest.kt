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

package io.element.android.features.location.impl

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SendLocationPresenterTest {

    private val room = FakeMatrixRoom()
    private val presenter = SendLocationPresenter(room)

    @Test
    fun `emits initial state`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            Truth.assertThat(awaitItem().mode).isEqualTo(SendLocationState.Mode.ALocation)
        }
    }

    @Test
    fun `share location event shares a location`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(SendLocationEvents.ShareLocation(1.0, 2.0))
            delay(1)
            Truth.assertThat(room.sendLocationCount).isEqualTo(1)
        }
    }
}
