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

package io.element.android.features.createroom.impl.root

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CreateRoomRootPresenterTests {

    @Test
    fun `present - initial state`() = runTest {
        val presenter = CreateRoomRootPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState)
        }
    }

    @Test
    fun `present - trigger action buttons`() = runTest {
        val presenter = CreateRoomRootPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(CreateRoomRootEvents.CreateRoom) // Not implemented yet
            initialState.eventSink(CreateRoomRootEvents.InvitePeople) // Not implemented yet
        }
    }

    @Test
    fun `present - update search query`() = runTest {
        val presenter = CreateRoomRootPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val matrixIdQuery = "@name:matrix.org"
            initialState.eventSink(CreateRoomRootEvents.UpdateSearchQuery(matrixIdQuery))
            assertThat(awaitItem().searchQuery).isEqualTo(matrixIdQuery)
            assertThat(awaitItem().searchResults).containsExactly(MatrixUser(UserId(matrixIdQuery)))

            val notMatrixIdQuery = "name"
            initialState.eventSink(CreateRoomRootEvents.UpdateSearchQuery(notMatrixIdQuery))
            assertThat(awaitItem().searchQuery).isEqualTo(notMatrixIdQuery)
            assertThat(awaitItem().searchResults).isEmpty()
        }
    }

    @Test
    fun `present - trigger start DM action`() = runTest {
        val presenter = CreateRoomRootPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val matrixUser = MatrixUser(UserId("@name:matrix.org"))
            initialState.eventSink(CreateRoomRootEvents.StartDM(matrixUser))
        }
    }
}
