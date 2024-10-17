/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.userprofile.shared

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.userprofile.api.UserProfileEvents
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId

open class UserProfileStateProvider : PreviewParameterProvider<UserProfileState> {
    override val values: Sequence<UserProfileState>
        get() = sequenceOf(
            aUserProfileState(),
            aUserProfileState(userName = null),
            aUserProfileState(isBlocked = AsyncData.Success(true)),
            aUserProfileState(displayConfirmationDialog = UserProfileState.ConfirmationDialog.Block),
            aUserProfileState(displayConfirmationDialog = UserProfileState.ConfirmationDialog.Unblock),
            aUserProfileState(isBlocked = AsyncData.Loading(true)),
            aUserProfileState(startDmActionState = AsyncAction.Loading),
            aUserProfileState(canCall = true),
            aUserProfileState(dmRoomId = null),
            // Add other states here
        )
}

fun aUserProfileState(
    userId: UserId = UserId("@daniel:domain.com"),
    userName: String? = "Daniel",
    avatarUrl: String? = null,
    isBlocked: AsyncData<Boolean> = AsyncData.Success(false),
    startDmActionState: AsyncAction<RoomId> = AsyncAction.Uninitialized,
    displayConfirmationDialog: UserProfileState.ConfirmationDialog? = null,
    isCurrentUser: Boolean = false,
    dmRoomId: RoomId? = null,
    canCall: Boolean = false,
    eventSink: (UserProfileEvents) -> Unit = {},
) = UserProfileState(
    userId = userId,
    userName = userName,
    avatarUrl = avatarUrl,
    isBlocked = isBlocked,
    startDmActionState = startDmActionState,
    displayConfirmationDialog = displayConfirmationDialog,
    isCurrentUser = isCurrentUser,
    dmRoomId = dmRoomId,
    canCall = canCall,
    eventSink = eventSink,
)
