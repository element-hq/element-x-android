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

package io.element.android.features.createroom.impl

import androidx.compose.runtime.mutableStateOf
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.CreatedRoom
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultStartDMActionTests {

    @Test
    fun `when dm is found, assert state is updated with given room id`() = runTest {
        val matrixClient = FakeMatrixClient().apply {
            givenFindDmResult(A_ROOM_ID)
        }
        val action = createStartDMAction(matrixClient)
        val state = mutableStateOf<Async<RoomId>>(Async.Uninitialized)
        action.execute(A_USER_ID, state)
        assertThat(state.value).isEqualTo(Async.Success(A_ROOM_ID))
    }

    @Test
    fun `when dm is not found, assert dm is created, state is updated with given room id and analytics get called`() = runTest {
        val matrixClient = FakeMatrixClient().apply {
            givenFindDmResult(null)
            givenCreateDmResult(Result.success(A_ROOM_ID))
        }
        val analyticsService = FakeAnalyticsService()
        val action = createStartDMAction(matrixClient, analyticsService)
        val state = mutableStateOf<Async<RoomId>>(Async.Uninitialized)
        action.execute(A_USER_ID, state)
        assertThat(state.value).isEqualTo(Async.Success(A_ROOM_ID))
        assertThat(analyticsService.capturedEvents).containsExactly(CreatedRoom(isDM = true))
    }

    @Test
    fun `when dm creation fails, assert state is updated with given error`() = runTest {
        val matrixClient = FakeMatrixClient().apply {
            givenFindDmResult(null)
            givenCreateDmResult(Result.failure(A_THROWABLE))
        }
        val action = createStartDMAction(matrixClient)
        val state = mutableStateOf<Async<RoomId>>(Async.Uninitialized)
        action.execute(A_USER_ID, state)
        assertThat(state.value).isEqualTo(Async.Failure<RoomId>(A_THROWABLE))
    }

    private fun createStartDMAction(
        matrixClient: MatrixClient = FakeMatrixClient(),
        analyticsService: AnalyticsService = FakeAnalyticsService(),
    ): DefaultStartDMAction {
        return DefaultStartDMAction(
            matrixClient = matrixClient,
            analyticsService = analyticsService,
        )
    }
}
