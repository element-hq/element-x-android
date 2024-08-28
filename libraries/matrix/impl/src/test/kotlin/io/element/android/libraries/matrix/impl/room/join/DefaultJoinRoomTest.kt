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

package io.element.android.libraries.matrix.impl.room.join

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.impl.analytics.toAnalyticsJoinedRoom
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SERVER_LIST
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomSummaryFilled
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultJoinRoomTest {
    @Test
    fun `when using roomId and there is no server names, the classic join room API is used`() = runTest {
        val roomSummary = aRoomSummaryFilled()
        val joinRoomLambda = lambdaRecorder { _: RoomId -> Result.success(roomSummary) }
        val joinRoomByIdOrAliasLambda = lambdaRecorder { _: RoomIdOrAlias, _: List<String> -> Result.success(roomSummary) }
        val roomResult = FakeMatrixRoom()
        val aTrigger = JoinedRoom.Trigger.MobilePermalink
        val client: MatrixClient = FakeMatrixClient().also {
            it.joinRoomLambda = joinRoomLambda
            it.joinRoomByIdOrAliasLambda = joinRoomByIdOrAliasLambda
            it.givenGetRoomResult(
                roomId = A_ROOM_ID,
                result = roomResult
            )
        }
        val analyticsService = FakeAnalyticsService()
        val sut = DefaultJoinRoom(
            client = client,
            analyticsService = analyticsService,
        )
        sut.invoke(A_ROOM_ID.toRoomIdOrAlias(), emptyList(), aTrigger)
        joinRoomByIdOrAliasLambda
            .assertions()
            .isNeverCalled()
        joinRoomLambda
            .assertions()
            .isCalledOnce()
            .with(
                value(A_ROOM_ID)
            )
        assertThat(analyticsService.capturedEvents).containsExactly(
            roomResult.toAnalyticsJoinedRoom(aTrigger)
        )
    }

    @Test
    fun `when using roomId and server names are available, joinRoomByIdOrAlias API is used`() = runTest {
        val roomSummary = aRoomSummaryFilled()
        val joinRoomLambda = lambdaRecorder { _: RoomId -> Result.success(roomSummary) }
        val joinRoomByIdOrAliasLambda = lambdaRecorder { _: RoomIdOrAlias, _: List<String> -> Result.success(roomSummary) }
        val roomResult = FakeMatrixRoom()
        val aTrigger = JoinedRoom.Trigger.MobilePermalink
        val client: MatrixClient = FakeMatrixClient().also {
            it.joinRoomLambda = joinRoomLambda
            it.joinRoomByIdOrAliasLambda = joinRoomByIdOrAliasLambda
            it.givenGetRoomResult(
                roomId = A_ROOM_ID,
                result = roomResult
            )
        }
        val analyticsService = FakeAnalyticsService()
        val sut = DefaultJoinRoom(
            client = client,
            analyticsService = analyticsService,
        )
        sut.invoke(A_ROOM_ID.toRoomIdOrAlias(), A_SERVER_LIST, aTrigger)
        joinRoomByIdOrAliasLambda
            .assertions()
            .isCalledOnce()
            .with(
                value(A_ROOM_ID.toRoomIdOrAlias()),
                value(A_SERVER_LIST)
            )
        joinRoomLambda
            .assertions()
            .isNeverCalled()
        assertThat(analyticsService.capturedEvents).containsExactly(
            roomResult.toAnalyticsJoinedRoom(aTrigger)
        )
    }

    @Test
    fun `when using roomAlias, joinRoomByIdOrAlias API is used`() = runTest {
        val roomSummary = aRoomSummaryFilled()
        val joinRoomLambda = lambdaRecorder { _: RoomId -> Result.success(roomSummary) }
        val joinRoomByIdOrAliasLambda = lambdaRecorder { _: RoomIdOrAlias, _: List<String> -> Result.success(roomSummary) }
        val roomResult = FakeMatrixRoom()
        val aTrigger = JoinedRoom.Trigger.MobilePermalink
        val client: MatrixClient = FakeMatrixClient().also {
            it.joinRoomLambda = joinRoomLambda
            it.joinRoomByIdOrAliasLambda = joinRoomByIdOrAliasLambda
            it.givenGetRoomResult(
                roomId = A_ROOM_ID,
                result = roomResult
            )
        }
        val analyticsService = FakeAnalyticsService()
        val sut = DefaultJoinRoom(
            client = client,
            analyticsService = analyticsService,
        )
        sut.invoke(A_ROOM_ALIAS.toRoomIdOrAlias(), A_SERVER_LIST, aTrigger)
        joinRoomByIdOrAliasLambda
            .assertions()
            .isCalledOnce()
            .with(
                value(A_ROOM_ALIAS.toRoomIdOrAlias()),
                value(emptyList<String>())
            )
        joinRoomLambda
            .assertions()
            .isNeverCalled()
        assertThat(analyticsService.capturedEvents).containsExactly(
            roomResult.toAnalyticsJoinedRoom(aTrigger)
        )
    }
}
