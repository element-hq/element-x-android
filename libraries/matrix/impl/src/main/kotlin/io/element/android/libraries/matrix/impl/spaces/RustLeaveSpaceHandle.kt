/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.spaces.LeaveSpaceHandle
import io.element.android.libraries.matrix.api.spaces.LeaveSpaceRoom
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import org.matrix.rustcomponents.sdk.LeaveSpaceHandle as RustLeaveSpaceHandle

class RustLeaveSpaceHandle(
    override val id: RoomId,
    private val spaceRoomMapper: SpaceRoomMapper,
    private val roomMembershipObserver: RoomMembershipObserver,
    sessionCoroutineScope: CoroutineScope,
    private val innerProvider: suspend () -> RustLeaveSpaceHandle,
) : LeaveSpaceHandle {
    private val inner = CompletableDeferred<RustLeaveSpaceHandle>()

    init {
        sessionCoroutineScope.launch {
            inner.complete(innerProvider())
        }
    }

    override suspend fun rooms(): Result<List<LeaveSpaceRoom>> = runCatchingExceptions {
        inner.await().rooms().map { leaveSpaceRoom ->
            LeaveSpaceRoom(
                spaceRoom = spaceRoomMapper.map(leaveSpaceRoom.spaceRoom),
                isLastAdmin = leaveSpaceRoom.isLastAdmin,
            )
        }
    }

    override suspend fun leave(roomIds: List<RoomId>): Result<Unit> = runCatchingExceptions {
        // Ensure the space is included and is the last room to be left
        val roomToLeave = roomIds - id + id
        inner.await().leave(roomToLeave.map { it.value })
    }.onSuccess {
        roomMembershipObserver.notifyUserLeftRoom(
            roomId = id,
            isSpace = true,
            membershipBeforeLeft = CurrentUserMembership.JOINED,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun close() {
        Timber.d("Destroying LeaveSpaceHandle $id")
        try {
            inner.getCompleted().destroy()
        } catch (_: Exception) {
            // Ignore, we just want to make sure it's completed
        }
    }
}
