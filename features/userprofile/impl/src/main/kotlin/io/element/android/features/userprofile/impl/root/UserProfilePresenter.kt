/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.userprofile.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.createroom.api.StartDMAction
import io.element.android.features.userprofile.api.UserProfileEvents
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.features.userprofile.api.UserProfileState.ConfirmationDialog
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UserProfilePresenter @AssistedInject constructor(
    @Assisted private val userId: UserId,
    private val client: MatrixClient,
    private val startDMAction: StartDMAction,
) : Presenter<UserProfileState> {
    @AssistedFactory
    interface Factory {
        fun create(userId: UserId): UserProfilePresenter
    }

    @Composable
    private fun getDmRoomId(): State<RoomId?> {
        return produceState<RoomId?>(initialValue = null) {
            value = client.findDM(userId)
        }
    }

    @Composable
    private fun getCanCall(roomId: RoomId?): State<Boolean> {
        return produceState(initialValue = false, roomId) {
            value = if (client.isMe(userId)) {
                false
            } else {
                roomId?.let { client.getRoom(it)?.canUserJoinCall(client.sessionId)?.getOrNull() == true }.orFalse()
            }
        }
    }

    @Composable
    override fun present(): UserProfileState {
        val coroutineScope = rememberCoroutineScope()
        val isCurrentUser = remember { client.isMe(userId) }
        var confirmationDialog by remember { mutableStateOf<ConfirmationDialog?>(null) }
        var userProfile by remember { mutableStateOf<MatrixUser?>(null) }
        val startDmActionState: MutableState<AsyncAction<RoomId>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val isBlocked: MutableState<AsyncData<Boolean>> = remember { mutableStateOf(AsyncData.Uninitialized) }
        val dmRoomId by getDmRoomId()
        val canCall by getCanCall(dmRoomId)
        LaunchedEffect(Unit) {
            client.ignoredUsersFlow
                .map { ignoredUsers -> userId in ignoredUsers }
                .distinctUntilChanged()
                .onEach { isBlocked.value = AsyncData.Success(it) }
                .launchIn(this)
        }
        LaunchedEffect(Unit) {
            userProfile = client.getProfile(userId).getOrNull()
        }

        fun handleEvents(event: UserProfileEvents) {
            when (event) {
                is UserProfileEvents.BlockUser -> {
                    if (event.needsConfirmation) {
                        confirmationDialog = ConfirmationDialog.Block
                    } else {
                        confirmationDialog = null
                        coroutineScope.blockUser(isBlocked)
                    }
                }
                is UserProfileEvents.UnblockUser -> {
                    if (event.needsConfirmation) {
                        confirmationDialog = ConfirmationDialog.Unblock
                    } else {
                        confirmationDialog = null
                        coroutineScope.unblockUser(isBlocked)
                    }
                }
                UserProfileEvents.ClearConfirmationDialog -> confirmationDialog = null
                UserProfileEvents.ClearBlockUserError -> {
                    isBlocked.value = AsyncData.Success(isBlocked.value.dataOrNull().orFalse())
                }
                UserProfileEvents.StartDM -> {
                    coroutineScope.launch {
                        startDMAction.execute(userId, startDmActionState)
                    }
                }
                UserProfileEvents.ClearStartDMState -> {
                    startDmActionState.value = AsyncAction.Uninitialized
                }
            }
        }

        return UserProfileState(
            userId = userId,
            userName = userProfile?.displayName,
            avatarUrl = userProfile?.avatarUrl,
            isBlocked = isBlocked.value,
            startDmActionState = startDmActionState.value,
            displayConfirmationDialog = confirmationDialog,
            isCurrentUser = isCurrentUser,
            dmRoomId = dmRoomId,
            canCall = canCall,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.blockUser(
        isBlockedState: MutableState<AsyncData<Boolean>>,
    ) = launch {
        isBlockedState.value = AsyncData.Loading(false)
        client.ignoreUser(userId)
            .onFailure {
                isBlockedState.value = AsyncData.Failure(it, false)
            }
        // Note: on success, ignoredUsersFlow will emit new item.
    }

    private fun CoroutineScope.unblockUser(
        isBlockedState: MutableState<AsyncData<Boolean>>,
    ) = launch {
        isBlockedState.value = AsyncData.Loading(true)
        client.unignoreUser(userId)
            .onFailure {
                isBlockedState.value = AsyncData.Failure(it, true)
            }
        // Note: on success, ignoredUsersFlow will emit new item.
    }
}
