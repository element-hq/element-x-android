/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomlist.impl.migration

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomlist.api.migration.MigrationScreenStore
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MigrationScreenPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isMigrating).isTrue()
        }
    }

    @Test
    fun `present - migration end`() = runTest {
        val matrixClient = FakeMatrixClient()
        val migrationScreenStore = InMemoryMigrationScreenStore()
        val presenter = createPresenter(matrixClient, migrationScreenStore)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isMigrating).isTrue()
            assertThat(migrationScreenStore.isMigrationScreenNeeded(A_SESSION_ID)).isTrue()
            // Simulate room list loaded
            (matrixClient.roomListService as FakeRoomListService).postState(RoomListService.State.Running)
            val nextState = awaitItem()
            assertThat(nextState.isMigrating).isFalse()
            assertThat(migrationScreenStore.isMigrationScreenNeeded(A_SESSION_ID)).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createPresenter(
        matrixClient: MatrixClient = FakeMatrixClient(),
        migrationScreenStore: MigrationScreenStore = InMemoryMigrationScreenStore(),
    ) = MigrationScreenPresenter(
        matrixClient,
        migrationScreenStore,
    )
}
