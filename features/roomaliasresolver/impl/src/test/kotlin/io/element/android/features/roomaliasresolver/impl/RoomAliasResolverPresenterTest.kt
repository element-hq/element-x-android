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

package io.element.android.features.roomaliasresolver.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SERVER_LIST
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RoomAliasResolverPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().resolveState.isUninitialized()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - resolve alias to roomId`() = runTest {
        val result = aResolvedRoomAlias()
        val client = FakeMatrixClient(
            resolveRoomAliasResult = { Result.success(result) }
        )
        val presenter = createPresenter(matrixClient = client)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().resolveState.isUninitialized()).isTrue()
            assertThat(awaitItem().resolveState.isLoading()).isTrue()
            val resultState = awaitItem()
            assertThat(resultState.roomAlias).isEqualTo(A_ROOM_ALIAS)
            assertThat(resultState.resolveState.dataOrNull()).isEqualTo(result)
        }
    }

    @Test
    fun `present - resolve alias error and retry`() = runTest {
        val client = FakeMatrixClient(
            resolveRoomAliasResult = { Result.failure(AN_EXCEPTION) }
        )
        val presenter = createPresenter(matrixClient = client)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().resolveState.isUninitialized()).isTrue()
            assertThat(awaitItem().resolveState.isLoading()).isTrue()
            val resultState = awaitItem()
            assertThat(resultState.resolveState.errorOrNull()).isEqualTo(AN_EXCEPTION)
            resultState.eventSink(RoomAliasResolverEvents.Retry)
            val retryLoadingState = awaitItem()
            assertThat(retryLoadingState.resolveState.isLoading()).isTrue()
            val retryState = awaitItem()
            assertThat(retryState.resolveState.errorOrNull()).isEqualTo(AN_EXCEPTION)
        }
    }

    private fun createPresenter(
        roomAlias: RoomAlias = A_ROOM_ALIAS,
        matrixClient: MatrixClient = FakeMatrixClient(),
    ) = RoomAliasResolverPresenter(
        roomAlias = roomAlias.value,
        matrixClient = matrixClient,
    )
}

internal fun aResolvedRoomAlias(
    roomId: RoomId = A_ROOM_ID,
    servers: List<String> = A_SERVER_LIST,
) = ResolvedRoomAlias(
    roomId = roomId,
    servers = servers,
)
