/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.userprofile.api

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId

data class UserProfileState(
    val userId: UserId,
    val userName: String?,
    val avatarUrl: String?,
    val isVerified: AsyncData<Boolean>,
    val isBlocked: AsyncData<Boolean>,
    val startDmActionState: AsyncAction<RoomId>,
    val displayConfirmationDialog: ConfirmationDialog?,
    val isCurrentUser: Boolean,
    val dmRoomId: RoomId?,
    val canCall: Boolean,
    val eventSink: (UserProfileEvents) -> Unit
) {
    enum class ConfirmationDialog {
        Block,
        Unblock
    }
}
