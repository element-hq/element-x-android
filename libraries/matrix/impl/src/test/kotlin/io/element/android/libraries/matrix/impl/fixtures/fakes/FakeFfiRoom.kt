/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomInfo
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.tests.testutils.lambda.lambdaError
import org.matrix.rustcomponents.sdk.EventTimelineItem
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomInfo
import org.matrix.rustcomponents.sdk.RoomMembersIterator

class FakeFfiRoom(
    private val roomId: RoomId = A_ROOM_ID,
    private val getMembers: () -> RoomMembersIterator = { lambdaError() },
    private val getMembersNoSync: () -> RoomMembersIterator = { lambdaError() },
    private val leaveLambda: () -> Unit = { lambdaError() },
    private val latestEventLambda: () -> EventTimelineItem? = { lambdaError() },
    private val roomInfo: RoomInfo = aRustRoomInfo(id = roomId.value),
) : Room(NoPointer) {
    override fun id(): String {
        return roomId.value
    }

    override suspend fun members(): RoomMembersIterator {
        return getMembers()
    }

    override suspend fun membersNoSync(): RoomMembersIterator {
        return getMembersNoSync()
    }

    override suspend fun leave() {
        leaveLambda()
    }

    override suspend fun roomInfo(): RoomInfo {
        return roomInfo
    }

    override suspend fun latestEvent(): EventTimelineItem? {
        return latestEventLambda()
    }

    override fun close() {
        // No-op
    }
}
