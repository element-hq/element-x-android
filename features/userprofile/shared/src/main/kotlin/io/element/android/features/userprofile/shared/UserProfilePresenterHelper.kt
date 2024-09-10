/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.userprofile.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UserProfilePresenterHelper(
    private val userId: UserId,
    private val client: MatrixClient,
) {
    @Composable
    fun getDmRoomId(): State<RoomId?> {
        return produceState<RoomId?>(initialValue = null) {
            value = client.findDM(userId)
        }
    }

    @Composable
    fun getCanCall(roomId: RoomId?): State<Boolean> {
        return produceState(initialValue = false, roomId) {
            value = if (client.isMe(userId)) {
                false
            } else {
                roomId?.let { client.getRoom(it)?.canUserJoinCall(client.sessionId)?.getOrNull() == true }.orFalse()
            }
        }
    }

    fun blockUser(
        scope: CoroutineScope,
        isBlockedState: MutableState<AsyncData<Boolean>>,
    ) = scope.launch {
        isBlockedState.value = AsyncData.Loading(false)
        client.ignoreUser(userId)
            .onFailure {
                isBlockedState.value = AsyncData.Failure(it, false)
            }
        // Note: on success, ignoredUserList will be updated.
    }

    fun unblockUser(
        scope: CoroutineScope,
        isBlockedState: MutableState<AsyncData<Boolean>>,
    ) = scope.launch {
        isBlockedState.value = AsyncData.Loading(true)
        client.unignoreUser(userId)
            .onFailure {
                isBlockedState.value = AsyncData.Failure(it, true)
            }
        // Note: on success, ignoredUserList will be updated.
    }
}
