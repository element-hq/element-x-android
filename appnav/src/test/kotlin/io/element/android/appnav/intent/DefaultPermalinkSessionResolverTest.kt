/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.intent

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Optional

class DefaultPermalinkSessionResolverTest {
    private val sessionIds = listOf(A_SESSION_ID, A_SESSION_ID_2)

    @Test
    fun `when only the second session has joined the room, then the second session is returned`() = runTest {
        val resolver = createResolver(
            A_SESSION_ID to aClient(A_SESSION_ID, membership = null),
            A_SESSION_ID_2 to aClient(A_SESSION_ID_2, membership = CurrentUserMembership.JOINED),
        )
        assertThat(resolver.resolve(RoomIdOrAlias.Id(A_ROOM_ID), sessionIds)).isEqualTo(A_SESSION_ID_2)
    }

    @Test
    fun `when both sessions have joined the room, then the most recently used session is returned`() = runTest {
        val resolver = createResolver(
            A_SESSION_ID to aClient(A_SESSION_ID, membership = CurrentUserMembership.JOINED),
            A_SESSION_ID_2 to aClient(A_SESSION_ID_2, membership = CurrentUserMembership.JOINED),
        )
        assertThat(resolver.resolve(RoomIdOrAlias.Id(A_ROOM_ID), sessionIds)).isEqualTo(A_SESSION_ID)
    }

    @Test
    fun `when no session has joined the room, then null is returned`() = runTest {
        val resolver = createResolver(
            A_SESSION_ID to aClient(A_SESSION_ID, membership = CurrentUserMembership.INVITED),
            A_SESSION_ID_2 to aClient(A_SESSION_ID_2, membership = CurrentUserMembership.LEFT),
        )
        assertThat(resolver.resolve(RoomIdOrAlias.Id(A_ROOM_ID), sessionIds)).isNull()
    }

    @Test
    fun `when the room is unknown for all sessions, then null is returned`() = runTest {
        val resolver = createResolver(
            A_SESSION_ID to aClient(A_SESSION_ID, membership = null),
            A_SESSION_ID_2 to aClient(A_SESSION_ID_2, membership = null),
        )
        assertThat(resolver.resolve(RoomIdOrAlias.Id(A_ROOM_ID), sessionIds)).isNull()
    }

    @Test
    fun `when a session cannot be restored, then it is skipped`() = runTest {
        val resolver = DefaultPermalinkSessionResolver(
            matrixClientProvider = FakeMatrixClientProvider { sessionId ->
                if (sessionId == A_SESSION_ID) {
                    Result.failure(AN_EXCEPTION)
                } else {
                    Result.success(aClient(A_SESSION_ID_2, membership = CurrentUserMembership.JOINED))
                }
            },
        )
        assertThat(resolver.resolve(RoomIdOrAlias.Id(A_ROOM_ID), sessionIds)).isEqualTo(A_SESSION_ID_2)
    }

    @Test
    fun `when the link contains an alias, then it is resolved and the membership is checked with the resolved room id`() = runTest {
        val resolveRoomAliasResult = lambdaRecorder<RoomAlias, Result<Optional<ResolvedRoomAlias>>> {
            Result.success(Optional.of(ResolvedRoomAlias(A_ROOM_ID_2, emptyList())))
        }
        val resolver = createResolver(
            A_SESSION_ID to aClient(A_SESSION_ID, membership = null, roomId = A_ROOM_ID_2, resolveRoomAliasResult = { resolveRoomAliasResult(it) }),
            A_SESSION_ID_2 to aClient(A_SESSION_ID_2, membership = CurrentUserMembership.JOINED, roomId = A_ROOM_ID_2),
        )
        assertThat(resolver.resolve(RoomIdOrAlias.Alias(A_ROOM_ALIAS), sessionIds)).isEqualTo(A_SESSION_ID_2)
        resolveRoomAliasResult.assertions().isCalledOnce()
    }

    @Test
    fun `when the alias cannot be resolved, then null is returned`() = runTest {
        val resolver = createResolver(
            A_SESSION_ID to aClient(A_SESSION_ID, membership = CurrentUserMembership.JOINED, resolveRoomAliasResult = { Result.failure(AN_EXCEPTION) }),
            A_SESSION_ID_2 to aClient(A_SESSION_ID_2, membership = CurrentUserMembership.JOINED, resolveRoomAliasResult = { Result.success(Optional.empty()) }),
        )
        assertThat(resolver.resolve(RoomIdOrAlias.Alias(A_ROOM_ALIAS), sessionIds)).isNull()
    }

    private fun createResolver(vararg clients: Pair<SessionId, MatrixClient>): DefaultPermalinkSessionResolver {
        val clientMap = clients.toMap()
        return DefaultPermalinkSessionResolver(
            matrixClientProvider = FakeMatrixClientProvider { sessionId -> Result.success(clientMap.getValue(sessionId)) },
        )
    }

    private fun aClient(
        sessionId: SessionId,
        membership: CurrentUserMembership?,
        roomId: RoomId = A_ROOM_ID,
        resolveRoomAliasResult: suspend (RoomAlias) -> Result<Optional<ResolvedRoomAlias>> = {
            Result.success(Optional.of(ResolvedRoomAlias(roomId, emptyList())))
        },
    ) = FakeMatrixClient(
        sessionId = sessionId,
        resolveRoomAliasResult = resolveRoomAliasResult,
    ).apply {
        getRoomInfoLambda = { requestedRoomId: RoomId ->
            val roomInfo: RoomInfo? = if (membership != null && requestedRoomId == roomId) {
                aRoomInfo(id = roomId, currentUserMembership = membership)
            } else {
                null
            }
            Result.success(roomInfo)
        }
    }
}
