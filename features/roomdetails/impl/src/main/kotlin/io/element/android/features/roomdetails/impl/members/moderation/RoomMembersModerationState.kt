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

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.collections.immutable.ImmutableList

data class RoomMembersModerationState(
    val selectedRoomMember: RoomMember?,
    val actions: ImmutableList<ModerationAction>,
    val kickUserAsyncAction: AsyncAction<Unit>,
    val banUserAsyncAction: AsyncAction<Unit>,
    val unbanUserAsyncAction: AsyncAction<Unit>,
    val canDisplayBannedUsers: Boolean,
    val eventSink: (RoomMembersModerationEvents) -> Unit,
)

sealed interface ModerationAction {
    data class DisplayProfile(val userId: UserId) : ModerationAction
    data class KickUser(val userId: UserId) : ModerationAction
    data class BanUser(val userId: UserId) : ModerationAction
}
