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

package io.element.android.features.roomlist

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomlist.model.RoomListEvents
import io.element.android.libraries.matrix.core.SessionId
import io.element.android.libraries.matrixtest.FakeMatrixClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomListPresenterTests {

    @Test
    fun `present - should start with no user and then load user with success`() = runTest {
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                SessionId("sessionId")
            ),
            LastMessageFormatter()
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.matrixUser).isNull()
            val withUserState = awaitItem()
            assertThat(withUserState.matrixUser).isNotNull()
        }
    }

    @Test
    fun `present - should filter room with success`() = runTest {
        val presenter = RoomListPresenter(
            FakeMatrixClient(
                SessionId("sessionId")
            ),
            LastMessageFormatter()
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            var initialState = awaitItem()
            val withUserState = awaitItem()
            assertThat(withUserState.filter).isEqualTo("")
            withUserState.eventSink.invoke(RoomListEvents.UpdateFilter("t"))
            val withFilterState = awaitItem()
            assertThat(withFilterState.filter).isEqualTo("t")
        }
    }
}
