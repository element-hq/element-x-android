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

package io.element.android.features.roomdetails.impl.members

import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.collections.immutable.ImmutableList

data class RoomMemberListState(
    val roomMembers: AsyncData<RoomMembers>,
    val searchQuery: String,
    val searchResults: SearchBarResultState<AsyncData<RoomMembers>>,
    val isSearchActive: Boolean,
    val canInvite: Boolean,
    val moderationState: RoomMembersModerationState,
    val eventSink: (RoomMemberListEvents) -> Unit,
)

data class RoomMembers(
    val invited: ImmutableList<RoomMember>,
    val joined: ImmutableList<RoomMember>,
    val banned: ImmutableList<RoomMember>,
)
