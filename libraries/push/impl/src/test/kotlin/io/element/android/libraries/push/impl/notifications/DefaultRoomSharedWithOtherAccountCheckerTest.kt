/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultRoomSharedWithOtherAccountCheckerTest {
    @Test
    fun `when there is only one session, the room is not joined by another account`() = runTest {
        val sut = createChecker(
            sessionIds = listOf(A_SESSION_ID),
            getClient = { error("Should not be called") },
        )
        assertThat(sut.isJoinedByOtherAccount(A_SESSION_ID, A_ROOM_ID)).isFalse()
    }

    @Test
    fun `when the other account has joined the room, the room is joined by another account`() = runTest {
        val sut = createChecker(
            sessionIds = listOf(A_SESSION_ID, A_SESSION_ID_2),
            getClient = { sessionId ->
                assertThat(sessionId).isEqualTo(A_SESSION_ID_2)
                Result.success(aClient(sessionId) { Result.success(aRoomInfo(id = A_ROOM_ID, currentUserMembership = CurrentUserMembership.JOINED)) })
            },
        )
        assertThat(sut.isJoinedByOtherAccount(A_SESSION_ID, A_ROOM_ID)).isTrue()
    }

    @Test
    fun `when the other account has not joined the room, the room is not joined by another account`() = runTest {
        val sut = createChecker(
            sessionIds = listOf(A_SESSION_ID, A_SESSION_ID_2),
            getClient = { sessionId ->
                Result.success(aClient(sessionId) { Result.success(null) })
            },
        )
        assertThat(sut.isJoinedByOtherAccount(A_SESSION_ID, A_ROOM_ID)).isFalse()
    }

    @Test
    fun `when the other account has left the room, the room is not joined by another account`() = runTest {
        val sut = createChecker(
            sessionIds = listOf(A_SESSION_ID, A_SESSION_ID_2),
            getClient = { sessionId ->
                Result.success(aClient(sessionId) { Result.success(aRoomInfo(id = A_ROOM_ID, currentUserMembership = CurrentUserMembership.LEFT)) })
            },
        )
        assertThat(sut.isJoinedByOtherAccount(A_SESSION_ID, A_ROOM_ID)).isFalse()
    }

    @Test
    fun `when the room info of the other account cannot be retrieved, the room is not joined by another account`() = runTest {
        val sut = createChecker(
            sessionIds = listOf(A_SESSION_ID, A_SESSION_ID_2),
            getClient = { sessionId ->
                Result.success(aClient(sessionId) { Result.failure(Exception("An error")) })
            },
        )
        assertThat(sut.isJoinedByOtherAccount(A_SESSION_ID, A_ROOM_ID)).isFalse()
    }

    @Test
    fun `when the other account cannot be restored, the room is not joined by another account`() = runTest {
        val sut = createChecker(
            sessionIds = listOf(A_SESSION_ID, A_SESSION_ID_2),
            getClient = { Result.failure(Exception("An error")) },
        )
        assertThat(sut.isJoinedByOtherAccount(A_SESSION_ID, A_ROOM_ID)).isFalse()
    }

    private fun aClient(
        sessionId: SessionId,
        getRoomInfoResult: (RoomId) -> Result<RoomInfo?>,
    ) = FakeMatrixClient(sessionId = sessionId).apply {
        getRoomInfoLambda = getRoomInfoResult
    }

    private fun createChecker(
        sessionIds: List<SessionId>,
        getClient: (SessionId) -> Result<MatrixClient>,
    ) = DefaultRoomSharedWithOtherAccountChecker(
        sessionStore = InMemorySessionStore(sessionIds.map { aSessionData(sessionId = it.value) }),
        matrixClientProvider = FakeMatrixClientProvider(getClient),
    )
}
