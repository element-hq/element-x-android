/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.features.createroom.api.StartDMAction
import io.element.android.features.userprofile.shared.UserProfileEvents
import io.element.android.features.userprofile.shared.UserProfilePresenterHelper
import io.element.android.features.userprofile.shared.UserProfileState
import io.element.android.features.userprofile.shared.UserProfileState.ConfirmationDialog
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.room.getRoomMemberAsState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RoomMemberDetailsPresenter @AssistedInject constructor(
    @Assisted private val roomMemberId: UserId,
    private val buildMeta: BuildMeta,
    private val client: MatrixClient,
    private val room: MatrixRoom,
    private val startDMAction: StartDMAction,
) : Presenter<UserProfileState> {
    interface Factory {
        fun create(roomMemberId: UserId): RoomMemberDetailsPresenter
    }

    private val userProfilePresenterHelper = UserProfilePresenterHelper(
        userId = roomMemberId,
        client = client,
    )

    @Composable
    override fun present(): UserProfileState {
        val coroutineScope = rememberCoroutineScope()
        var confirmationDialog by remember { mutableStateOf<ConfirmationDialog?>(null) }
        val roomMember by room.getRoomMemberAsState(roomMemberId)
        var userProfile by remember { mutableStateOf<MatrixUser?>(null) }
        val startDmActionState: MutableState<AsyncAction<RoomId>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val isBlocked: MutableState<AsyncData<Boolean>> = remember { mutableStateOf(AsyncData.Uninitialized) }
        val isCurrentUser = remember { client.isMe(roomMemberId) }
        val dmRoomId by userProfilePresenterHelper.getDmRoomId()
        val canCall by userProfilePresenterHelper.getCanCall(dmRoomId)
        LaunchedEffect(Unit) {
            client.ignoredUsersFlow
                .map { ignoredUsers -> roomMemberId in ignoredUsers }
                .distinctUntilChanged()
                .onEach { isBlocked.value = AsyncData.Success(it) }
                .launchIn(this)
        }
        LaunchedEffect(Unit) {
            // Update room member info when opening this screen
            // We don't need to assign the result as it will be automatically propagated by `room.getRoomMemberAsState`
            room.getUpdatedMember(roomMemberId)
                .onFailure {
                    // Not a member of the room, try to get the user profile
                    userProfile = client.getProfile(roomMemberId).getOrNull()
                }
        }

        fun handleEvents(event: UserProfileEvents) {
            when (event) {
                is UserProfileEvents.BlockUser -> {
                    if (event.needsConfirmation) {
                        confirmationDialog = ConfirmationDialog.Block
                    } else {
                        confirmationDialog = null
                        userProfilePresenterHelper.blockUser(coroutineScope, isBlocked)
                    }
                }
                is UserProfileEvents.UnblockUser -> {
                    if (event.needsConfirmation) {
                        confirmationDialog = ConfirmationDialog.Unblock
                    } else {
                        confirmationDialog = null
                        userProfilePresenterHelper.unblockUser(coroutineScope, isBlocked)
                    }
                }
                UserProfileEvents.ClearConfirmationDialog -> confirmationDialog = null
                UserProfileEvents.ClearBlockUserError -> {
                    isBlocked.value = AsyncData.Success(isBlocked.value.dataOrNull().orFalse())
                }
                UserProfileEvents.StartDM -> {
                    coroutineScope.launch {
                        startDMAction.execute(roomMemberId, startDmActionState)
                    }
                }
                UserProfileEvents.ClearStartDMState -> {
                    startDmActionState.value = AsyncAction.Uninitialized
                }
            }
        }

        val userName: String? by produceState(
            initialValue = roomMember?.displayName ?: userProfile?.displayName,
            key1 = roomMember,
            key2 = userProfile,
        ) {
            value = room.userDisplayName(roomMemberId)
                .fold(
                    onSuccess = { it },
                    onFailure = {
                        // Fallback to user profile
                        userProfile?.displayName
                    }
                )
        }

        val userAvatar: String? by produceState(
            initialValue = roomMember?.avatarUrl ?: userProfile?.avatarUrl,
            key1 = roomMember,
            key2 = userProfile,
        ) {
            value = room.userAvatarUrl(roomMemberId)
                .fold(
                    onSuccess = { it },
                    onFailure = {
                        // Fallback to user profile
                        userProfile?.avatarUrl
                    }
                )
        }

        return UserProfileState(
            isDebugBuild = buildMeta.isDebuggable,
            userId = roomMemberId,
            userName = userName,
            avatarUrl = userAvatar,
            isBlocked = isBlocked.value,
            startDmActionState = startDmActionState.value,
            displayConfirmationDialog = confirmationDialog,
            isCurrentUser = isCurrentUser,
            dmRoomId = dmRoomId,
            canCall = canCall,
            eventSink = ::handleEvents
        )
    }
}
