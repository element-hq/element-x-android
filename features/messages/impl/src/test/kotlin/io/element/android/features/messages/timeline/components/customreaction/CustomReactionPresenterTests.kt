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

package io.element.android.features.messages.timeline.components.customreaction

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionEvents
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionPresenter
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CustomReactionPresenterTests {

    private val presenter = CustomReactionPresenter()

    @Test
    fun `present - handle selecting and de-selecting an event`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.selectedEventId).isNull()

            initialState.eventSink(CustomReactionEvents.UpdateSelectedEvent(AN_EVENT_ID))
            assertThat(awaitItem().selectedEventId).isEqualTo(AN_EVENT_ID)

            initialState.eventSink(CustomReactionEvents.UpdateSelectedEvent(null))
            assertThat(awaitItem().selectedEventId).isNull()
        }
    }
}
