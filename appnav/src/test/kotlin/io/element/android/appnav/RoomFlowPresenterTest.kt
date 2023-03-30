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

package io.element.android.appnav

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.timeline.FakeMatrixTimeline
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomFlowPresenterTest {

    @Test
    fun `present - makes sure timeline is initialized and disposed`() = runTest {
        val fakeTimeline = FakeMatrixTimeline()
        val presenter = RoomFlowPresenter(
            room = FakeMatrixRoom(matrixTimeline = fakeTimeline),
        )
        Truth.assertThat(fakeTimeline.isInitialized).isFalse()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            // Was initialized
            Truth.assertThat(fakeTimeline.isInitialized).isTrue()
            cancelAndConsumeRemainingEvents()
            // Was disposed
            Truth.assertThat(fakeTimeline.isInitialized).isFalse()
        }
        Truth.assertThat(fakeTimeline.isInitialized).isFalse()
    }

}
