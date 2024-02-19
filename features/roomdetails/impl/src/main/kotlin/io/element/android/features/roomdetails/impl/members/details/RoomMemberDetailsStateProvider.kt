/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.roomdetails.impl.members.details

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.RoomId

open class RoomMemberDetailsStateProvider : PreviewParameterProvider<RoomMemberDetailsState> {
    override val values: Sequence<RoomMemberDetailsState>
        get() = sequenceOf(
            aRoomMemberDetailsState(),
            aRoomMemberDetailsState(userName = null),
            aRoomMemberDetailsState(isBlocked = AsyncData.Success(true)),
            aRoomMemberDetailsState(displayConfirmationDialog = RoomMemberDetailsState.ConfirmationDialog.Block),
            aRoomMemberDetailsState(displayConfirmationDialog = RoomMemberDetailsState.ConfirmationDialog.Unblock),
            aRoomMemberDetailsState(isBlocked = AsyncData.Loading(true)),
            aRoomMemberDetailsState(startDmActionState = AsyncAction.Loading),
            // Add other states here
        )
}

fun aRoomMemberDetailsState(
    userId: String = "@daniel:domain.com",
    userName: String? = "Daniel",
    avatarUrl: String? = null,
    isBlocked: AsyncData<Boolean> = AsyncData.Success(false),
    startDmActionState: AsyncAction<RoomId> = AsyncAction.Uninitialized,
    displayConfirmationDialog: RoomMemberDetailsState.ConfirmationDialog? = null,
    isCurrentUser: Boolean = false,
    eventSink: (RoomMemberDetailsEvents) -> Unit = {},
) = RoomMemberDetailsState(
    userId = userId,
    userName = userName,
    avatarUrl = avatarUrl,
    isBlocked = isBlocked,
    startDmActionState = startDmActionState,
    displayConfirmationDialog = displayConfirmationDialog,
    isCurrentUser = isCurrentUser,
    eventSink = eventSink,
)
