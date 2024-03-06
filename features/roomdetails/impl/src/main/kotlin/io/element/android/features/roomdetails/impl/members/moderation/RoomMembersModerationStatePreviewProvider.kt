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

package io.element.android.features.roomdetails.impl.members.moderation

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roomdetails.impl.members.anAlice
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

class RoomMembersModerationStatePreviewProvider : PreviewParameterProvider<RoomMembersModerationState> {
    override val values: Sequence<RoomMembersModerationState>
        get() = sequenceOf(
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                actions = persistentListOf(
                    ModerationAction.DisplayProfile(anAlice().userId),
                ),
            ),
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                actions = persistentListOf(
                    ModerationAction.DisplayProfile(anAlice().userId),
                    ModerationAction.KickUser(userId = anAlice().userId),
                ),
            ),
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                actions = persistentListOf(
                    ModerationAction.DisplayProfile(anAlice().userId),
                    ModerationAction.KickUser(userId = anAlice().userId),
                    ModerationAction.BanUser(userId = anAlice().userId),
                ),
            ),
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                kickUserAsyncAction = AsyncAction.Loading,
            ),
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                banUserAsyncAction = AsyncAction.Loading,
            ),
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                unbanUserAsyncAction = AsyncAction.Loading,
            ),
            aRoomMembersModerationState(
                kickUserAsyncAction = AsyncAction.Failure(Exception("Failed to kick user")),
                banUserAsyncAction = AsyncAction.Failure(Exception("Failed to ban user")),
                unbanUserAsyncAction = AsyncAction.Failure(Exception("Failed to unban user")),
            ),
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                banUserAsyncAction = AsyncAction.Confirming,
            ),
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                unbanUserAsyncAction = AsyncAction.Confirming,
            ),
            aRoomMembersModerationState(
                kickUserAsyncAction = AsyncAction.Success(Unit),
                banUserAsyncAction = AsyncAction.Success(Unit),
                unbanUserAsyncAction = AsyncAction.Success(Unit),
            ),
        )
}

fun aRoomMembersModerationState(
    selectedRoomMember: RoomMember? = null,
    actions: List<ModerationAction> = emptyList(),
    kickUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    banUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    unbanUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    canDisplayBannedUsers: Boolean = false,
    eventSink: (RoomMembersModerationEvents) -> Unit = {},
) = RoomMembersModerationState(
    selectedRoomMember = selectedRoomMember,
    actions = actions.toPersistentList(),
    kickUserAsyncAction = kickUserAsyncAction,
    banUserAsyncAction = banUserAsyncAction,
    unbanUserAsyncAction = unbanUserAsyncAction,
    canDisplayBannedUsers = canDisplayBannedUsers,
    eventSink = eventSink,
)
