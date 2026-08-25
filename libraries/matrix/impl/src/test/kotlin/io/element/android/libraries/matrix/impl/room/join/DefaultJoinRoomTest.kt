/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.join

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.tombstone.PredecessorRoom
import io.element.android.libraries.matrix.impl.analytics.toAnalyticsJoinedRoom
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SERVER_LIST
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultJoinRoomTest {
    @Test
    fun `when using roomId and there is no server names, the classic join room API is used`() = runTest {
        val roomInfo = aRoomInfo()
        val joinRoomLambda = lambdaRecorder { _: RoomId -> Result.success(roomInfo) }
        val joinRoomByIdOrAliasLambda = lambdaRecorder { _: RoomIdOrAlias, _: List<String> -> Result.success(roomInfo) }
        val roomResult = FakeBaseRoom().apply {
            givenRoomInfo(aRoomInfo())
        }
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
        val roomInfo = aRoomInfo()
        val joinRoomLambda = lambdaRecorder { _: RoomId -> Result.success(roomInfo) }
        val joinRoomByIdOrAliasLambda = lambdaRecorder { _: RoomIdOrAlias, _: List<String> -> Result.success(roomInfo) }
        val roomResult = FakeBaseRoom().apply {
            givenRoomInfo(aRoomInfo())
        }
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
        val roomInfo = aRoomInfo()
        val joinRoomLambda = lambdaRecorder { _: RoomId -> Result.success(roomInfo) }
        val joinRoomByIdOrAliasLambda = lambdaRecorder { _: RoomIdOrAlias, _: List<String> -> Result.success(roomInfo) }
        val roomResult = FakeBaseRoom().apply {
            givenRoomInfo(aRoomInfo())
        }
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

    @Test
    fun `joining the successor of a room with its own notification mode carries that mode over`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService(
            initialRoomMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
            initialRoomModeIsDefault = false,
        )
        val sut = aJoinRoom(
            notificationSettingsService = notificationSettingsService,
            predecessorRoom = PredecessorRoom(roomId = A_ROOM_ID_2),
        )

        sut.invoke(A_ROOM_ID.toRoomIdOrAlias(), emptyList(), JoinedRoom.Trigger.MobilePermalink)

        assertThat(notificationSettingsService.setRoomNotificationModeCalls)
            .containsExactly(A_ROOM_ID to RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)
    }

    @Test
    fun `joining a room without a predecessor leaves its notification mode alone`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService(
            initialRoomMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
            initialRoomModeIsDefault = false,
        )
        val sut = aJoinRoom(
            notificationSettingsService = notificationSettingsService,
            predecessorRoom = null,
        )

        sut.invoke(A_ROOM_ID.toRoomIdOrAlias(), emptyList(), JoinedRoom.Trigger.MobilePermalink)

        assertThat(notificationSettingsService.setRoomNotificationModeCalls).isEmpty()
    }

    @Test
    fun `joining the successor of a room using the default notification mode leaves the default in place`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService(
            initialRoomModeIsDefault = true,
        )
        val sut = aJoinRoom(
            notificationSettingsService = notificationSettingsService,
            predecessorRoom = PredecessorRoom(roomId = A_ROOM_ID_2),
        )

        sut.invoke(A_ROOM_ID.toRoomIdOrAlias(), emptyList(), JoinedRoom.Trigger.MobilePermalink)

        assertThat(notificationSettingsService.setRoomNotificationModeCalls).isEmpty()
    }

    private fun aJoinRoom(
        notificationSettingsService: FakeNotificationSettingsService,
        predecessorRoom: PredecessorRoom?,
    ): DefaultJoinRoom {
        val roomInfo = aRoomInfo(id = A_ROOM_ID)
        val client = FakeMatrixClient(notificationSettingsService = notificationSettingsService).also {
            it.joinRoomLambda = { Result.success(roomInfo) }
            it.givenGetRoomResult(
                roomId = A_ROOM_ID,
                result = FakeBaseRoom(predecessorRoomResult = { predecessorRoom }).apply { givenRoomInfo(roomInfo) },
            )
            it.givenGetRoomResult(
                roomId = A_ROOM_ID_2,
                result = FakeBaseRoom().apply { givenRoomInfo(aRoomInfo(id = A_ROOM_ID_2)) },
            )
        }
        return DefaultJoinRoom(
            client = client,
            analyticsService = FakeAnalyticsService(),
        )
    }
}
