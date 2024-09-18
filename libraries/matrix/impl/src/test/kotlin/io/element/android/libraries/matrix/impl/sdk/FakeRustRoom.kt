/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.sdk

import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomMembersIterator

class FakeRustRoom(
    private val getMembers: () -> RoomMembersIterator = { FakeRustRoomMembersIterator() },
    private val getMembersNoSync: () -> RoomMembersIterator = { FakeRustRoomMembersIterator() },
) : Room(NoPointer) {
    var membersCallCount = 0
    var membersNoSyncCallCount = 0
    override suspend fun members(): RoomMembersIterator {
        membersCallCount++
        return getMembers()
    }

    override suspend fun membersNoSync(): RoomMembersIterator {
        membersNoSyncCallCount++
        return getMembersNoSync()
    }
}
