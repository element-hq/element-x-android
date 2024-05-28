/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.preferences.impl.blockedusers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import kotlinx.collections.immutable.toPersistentList

class BlockedUsersStatePreviewProvider : PreviewParameterProvider<BlockedUsersState> {
    override val values: Sequence<BlockedUsersState>
        get() = sequenceOf(
            aBlockedUsersState(),
            aBlockedUsersState(blockedUsers = aMatrixUserList().map { it.copy(displayName = null, avatarUrl = null) }),
            aBlockedUsersState(blockedUsers = emptyList()),
            aBlockedUsersState(unblockUserAction = AsyncAction.Confirming),
            // Sadly there's no good way to preview Loading or Failure states since they're presented with an animation
            // All these 3 screen states will be displayed as the Uninitialized one
            aBlockedUsersState(unblockUserAction = AsyncAction.Loading),
            aBlockedUsersState(unblockUserAction = AsyncAction.Failure(Throwable("Failed to unblock user"))),
            aBlockedUsersState(unblockUserAction = AsyncAction.Success(Unit)),
        )
}

internal fun aBlockedUsersState(
    blockedUsers: List<MatrixUser> = aMatrixUserList(),
    unblockUserAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
): BlockedUsersState {
    return BlockedUsersState(
        blockedUsers = blockedUsers.toPersistentList(),
        unblockUserAction = unblockUserAction,
        eventSink = {},
    )
}
