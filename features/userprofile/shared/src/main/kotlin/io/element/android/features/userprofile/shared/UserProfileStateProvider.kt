/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.userprofile.shared

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.startchat.api.ConfirmingStartDmWithMatrixUser
import io.element.android.features.userprofile.api.UserProfileEvents
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.features.userprofile.api.UserProfileVerificationState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.components.aMatrixUser

open class UserProfileStateProvider : PreviewParameterProvider<UserProfileState> {
    override val values: Sequence<UserProfileState>
        get() = sequenceOf(
            aUserProfileState(),
            aUserProfileState(userName = null),
            aUserProfileState(isBlocked = AsyncData.Success(true), verificationState = UserProfileVerificationState.VERIFIED),
            aUserProfileState(displayConfirmationDialog = UserProfileState.ConfirmationDialog.Block),
            aUserProfileState(displayConfirmationDialog = UserProfileState.ConfirmationDialog.Unblock),
            aUserProfileState(isBlocked = AsyncData.Loading(true), verificationState = UserProfileVerificationState.UNKNOWN),
            aUserProfileState(startDmActionState = AsyncAction.Loading),
            aUserProfileState(canCall = true),
            aUserProfileState(startDmActionState = ConfirmingStartDmWithMatrixUser(aMatrixUser())),
            aUserProfileState(verificationState = UserProfileVerificationState.VERIFICATION_VIOLATION),
        )
}

fun aUserProfileState(
    userId: UserId = UserId("@daniel:domain.com"),
    userName: String? = "Daniel",
    avatarUrl: String? = null,
    isBlocked: AsyncData<Boolean> = AsyncData.Success(false),
    verificationState: UserProfileVerificationState = UserProfileVerificationState.UNVERIFIED,
    startDmActionState: AsyncAction<RoomId> = AsyncAction.Uninitialized,
    displayConfirmationDialog: UserProfileState.ConfirmationDialog? = null,
    isCurrentUser: Boolean = false,
    dmRoomId: RoomId? = null,
    canCall: Boolean = false,
    snackbarMessage: SnackbarMessage? = null,
    eventSink: (UserProfileEvents) -> Unit = {},
) = UserProfileState(
    userId = userId,
    userName = userName,
    avatarUrl = avatarUrl,
    isBlocked = isBlocked,
    verificationState = verificationState,
    startDmActionState = startDmActionState,
    displayConfirmationDialog = displayConfirmationDialog,
    isCurrentUser = isCurrentUser,
    dmRoomId = dmRoomId,
    canCall = canCall,
    snackbarMessage = snackbarMessage,
    eventSink = eventSink,
)
