/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomInfo
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.matrix.rustcomponents.sdk.LatestEventValue
import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomInfo
import org.matrix.rustcomponents.sdk.RoomInfoListener
import org.matrix.rustcomponents.sdk.RoomMembersIterator
import org.matrix.rustcomponents.sdk.TaskHandle
import uniffi.matrix_sdk.RoomMemberRole

@OptIn(DelicateCoroutinesApi::class)
class FakeFfiRoom(
    private val roomId: RoomId = A_ROOM_ID,
    private val getMembers: () -> RoomMembersIterator = { lambdaError() },
    private val getMembersNoSync: () -> RoomMembersIterator = { lambdaError() },
    private val leaveLambda: () -> Unit = { lambdaError() },
    private val latestEventLambda: () -> LatestEventValue = { lambdaError() },
    private val suggestedRoleForUserLambda: (String) -> RoomMemberRole = { lambdaError() },
    private val roomInfo: RoomInfo = aRustRoomInfo(id = roomId.value),
    private val testScope: CoroutineScope = GlobalScope,
) : Room(NoHandle) {
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

    override suspend fun latestEvent(): LatestEventValue {
        return latestEventLambda()
    }

    override suspend fun suggestedRoleForUser(userId: String): RoomMemberRole {
        return suggestedRoleForUserLambda(userId)
    }

    private var roomInfoUpdatesFlow = MutableStateFlow(roomInfo)
    override fun subscribeToRoomInfoUpdates(listener: RoomInfoListener): TaskHandle {
        roomInfoUpdatesFlow
            // Drop the first item to match the SDK's behavior
            .drop(1)
            .onEach { listener.call(it) }
            .launchIn(testScope)
        return FakeFfiTaskHandle()
    }

    fun givenRoomInfoUpdate(newRoomInfo: RoomInfo) {
        roomInfoUpdatesFlow.value = newRoomInfo
    }

    override fun close() {
        // No-op
    }
}
