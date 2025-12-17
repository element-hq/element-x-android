/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.features.invite.test.InMemorySeenInvitesStore
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.join.FakeJoinRoom
import io.element.android.libraries.push.test.notifications.FakeNotificationCleaner
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultAcceptInviteTest {
    private val roomId = A_ROOM_ID
    private val client = FakeMatrixClient()
    private val seenInvitesStore = InMemorySeenInvitesStore(initialRoomIds = setOf(roomId))

    private val clearMembershipNotificationForRoomLambda =
        lambdaRecorder<SessionId, RoomId, Unit> { _, _ -> }
    private val notificationCleaner =
        FakeNotificationCleaner(clearMembershipNotificationForRoomLambda = clearMembershipNotificationForRoomLambda)

    @Test
    fun `accept invite success scenario`() = runTest {
        val joinRoomLambda =
            lambdaRecorder<RoomIdOrAlias, List<String>, JoinedRoom.Trigger, Result<Unit>> { _, _, _ ->
                Result.success(Unit)
            }

        val acceptInvite = DefaultAcceptInvite(
            client = client,
            notificationCleaner = notificationCleaner,
            joinRoom = FakeJoinRoom(lambda = joinRoomLambda),
            seenInvitesStore = seenInvitesStore
        )

        val result = acceptInvite(roomId)

        assertThat(result.isSuccess).isTrue()

        assert(joinRoomLambda)
            .isCalledOnce()
            .with(value(roomId.toRoomIdOrAlias()), any(), any())

        assert(clearMembershipNotificationForRoomLambda)
            .isCalledOnce()
            .with(value(client.sessionId), value(roomId))

        assertThat(seenInvitesStore.seenRoomIds().first()).isEmpty()
    }

    @Test
    fun `accept invite failure scenario`() = runTest {
        val joinRoomLambda =
            lambdaRecorder<RoomIdOrAlias, List<String>, JoinedRoom.Trigger, Result<Unit>> { _, _, _ ->
                Result.failure(RuntimeException("Join room failed"))
            }

        val acceptInvite = DefaultAcceptInvite(
            client = client,
            notificationCleaner = notificationCleaner,
            joinRoom = FakeJoinRoom(lambda = joinRoomLambda),
            seenInvitesStore = seenInvitesStore
        )

        val result = acceptInvite(roomId)

        assertThat(result.isFailure).isTrue()

        assert(joinRoomLambda)
            .isCalledOnce()
            .with(value(roomId.toRoomIdOrAlias()), any(), any())

        assert(clearMembershipNotificationForRoomLambda).isNeverCalled()

        assertThat(seenInvitesStore.seenRoomIds().first()).containsExactly(roomId)
    }
}
