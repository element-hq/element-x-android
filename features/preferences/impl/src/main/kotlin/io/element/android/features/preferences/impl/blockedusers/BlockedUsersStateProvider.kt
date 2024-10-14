/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.blockedusers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import kotlinx.collections.immutable.toPersistentList

class BlockedUsersStateProvider : PreviewParameterProvider<BlockedUsersState> {
    override val values: Sequence<BlockedUsersState>
        get() = sequenceOf(
            aBlockedUsersState(),
            aBlockedUsersState(blockedUsers = aMatrixUserList().map { it.copy(displayName = null, avatarUrl = null) }),
            aBlockedUsersState(blockedUsers = emptyList()),
            aBlockedUsersState(unblockUserAction = AsyncAction.ConfirmingNoParams),
            aBlockedUsersState(unblockUserAction = AsyncAction.Loading),
            aBlockedUsersState(unblockUserAction = AsyncAction.Failure(Throwable("Failed to unblock user"))),
            aBlockedUsersState(unblockUserAction = AsyncAction.Success(Unit)),
        )
}

internal fun aBlockedUsersState(
    blockedUsers: List<MatrixUser> = aMatrixUserList(),
    unblockUserAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (BlockedUsersEvents) -> Unit = {},
): BlockedUsersState {
    return BlockedUsersState(
        blockedUsers = blockedUsers.toPersistentList(),
        unblockUserAction = unblockUserAction,
        eventSink = eventSink,
    )
}
