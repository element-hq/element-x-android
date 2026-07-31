/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications.channels

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.notifications.RoomNotificationChannelManager
import io.element.android.tests.testutils.lambda.lambdaError

class FakeRoomNotificationChannelManager(
    private val getChannelIdForRoomLambda: (SessionId, RoomId, String, Boolean, Boolean) -> String = { _, _, _, _, _ -> lambdaError() },
    private val clearRoomChannelLambda: (SessionId, RoomId) -> Unit = { _, _ -> lambdaError() },
    private val pruneChannelsForSessionLambda: (SessionId, Set<RoomId>) -> Unit = { _, _ -> lambdaError() },
    private val clearAllChannelsForSessionLambda: (SessionId) -> Unit = { lambdaError() },
    private val pruneInactiveChannelsLambda: (SessionId) -> Unit = { lambdaError() },
) : RoomNotificationChannelManager {
    override suspend fun getChannelIdForRoom(sessionId: SessionId, roomId: RoomId, roomDisplayName: String, isDm: Boolean, noisy: Boolean): String =
        getChannelIdForRoomLambda(sessionId, roomId, roomDisplayName, isDm, noisy)

    override suspend fun clearRoomChannel(sessionId: SessionId, roomId: RoomId) {
        clearRoomChannelLambda(sessionId, roomId)
    }

    override suspend fun pruneChannelsForSession(sessionId: SessionId, roomIds: Set<RoomId>) {
        pruneChannelsForSessionLambda(sessionId, roomIds)
    }

    override suspend fun clearAllChannelsForSession(sessionId: SessionId) {
        clearAllChannelsForSessionLambda(sessionId)
    }

    override suspend fun pruneInactiveChannels(sessionId: SessionId) {
        pruneInactiveChannelsLambda(sessionId)
    }
}
