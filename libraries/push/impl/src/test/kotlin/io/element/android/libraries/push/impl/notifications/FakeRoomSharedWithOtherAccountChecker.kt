/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

class FakeRoomSharedWithOtherAccountChecker(
    private val isJoinedByOtherAccountResult: (SessionId, RoomId) -> Boolean = { _, _ -> false },
) : RoomSharedWithOtherAccountChecker {
    override suspend fun isJoinedByOtherAccount(sessionId: SessionId, roomId: RoomId): Boolean {
        return isJoinedByOtherAccountResult(sessionId, roomId)
    }
}
