/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.intent

import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.tests.testutils.lambda.lambdaError

class FakePermalinkSessionResolver(
    private val resolveForRoomResult: (RoomIdOrAlias, List<SessionId>) -> SessionId? = { _, _ -> lambdaError() },
    private val resolveForUserResult: (UserId, List<SessionId>) -> SessionId? = { _, _ -> lambdaError() },
) : PermalinkSessionResolver {
    override suspend fun resolveForRoom(roomIdOrAlias: RoomIdOrAlias, sessionIds: List<SessionId>): SessionId? {
        return resolveForRoomResult(roomIdOrAlias, sessionIds)
    }

    override suspend fun resolveForUser(userId: UserId, sessionIds: List<SessionId>): SessionId? {
        return resolveForUserResult(userId, sessionIds)
    }
}
