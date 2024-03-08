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

package io.element.android.features.roomdetails.impl.rolesandpermissions.permissions

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap

class ChangeRoomPermissionsStatePreviewProvider : PreviewParameterProvider<ChangeRoomPermissionsState> {
    override val values: Sequence<ChangeRoomPermissionsState>
        get() = sequenceOf(
            aChangeRoomPermissionsState(section = ChangeRoomPermissionsSection.RoomDetails),
            aChangeRoomPermissionsState(section = ChangeRoomPermissionsSection.MessagesAndContent),
            aChangeRoomPermissionsState(section = ChangeRoomPermissionsSection.MembershipModeration),
            aChangeRoomPermissionsState(section = ChangeRoomPermissionsSection.RoomDetails, hasChanges = true),
            aChangeRoomPermissionsState(section = ChangeRoomPermissionsSection.RoomDetails, hasChanges = true, saveAction = AsyncAction.Loading),
            aChangeRoomPermissionsState(
                section = ChangeRoomPermissionsSection.RoomDetails,
                hasChanges = true,
                saveAction = AsyncAction.Failure(IllegalStateException("Failed to save changes"))
            ),
            aChangeRoomPermissionsState(section = ChangeRoomPermissionsSection.RoomDetails, hasChanges = true, confirmExitAction = AsyncAction.Confirming),
        )
}

internal fun aChangeRoomPermissionsState(
    section: ChangeRoomPermissionsSection,
    currentPermissions: Map<RoomPermissionsItem, RoomMember.Role> = permissionsForSection(section),
    items: List<RoomPermissionsItem> = itemsForSection(section),
    hasChanges: Boolean = false,
    saveAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    confirmExitAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (ChangeRoomPermissionsEvent) -> Unit = {},
) = ChangeRoomPermissionsState(
    section = section,
    currentPermissions = currentPermissions.toPersistentMap(),
    items = items.toPersistentList(),
    hasChanges = hasChanges,
    saveAction = saveAction,
    confirmExitAction = confirmExitAction,
    eventSink = eventSink,
)

private fun itemsForSection(section: ChangeRoomPermissionsSection): ImmutableList<RoomPermissionsItem> {
    return when (section) {
        ChangeRoomPermissionsSection.RoomDetails -> persistentListOf(
            RoomPermissionsItem.ROOM_NAME,
            RoomPermissionsItem.ROOM_AVATAR,
            RoomPermissionsItem.ROOM_TOPIC,
        )
        ChangeRoomPermissionsSection.MessagesAndContent -> persistentListOf(
            RoomPermissionsItem.SEND_EVENTS,
            RoomPermissionsItem.REDACT_EVENTS,
        )
        ChangeRoomPermissionsSection.MembershipModeration -> persistentListOf(
            RoomPermissionsItem.INVITE,
            RoomPermissionsItem.KICK,
            RoomPermissionsItem.BAN,
        )
    }
}

private fun permissionsForSection(section: ChangeRoomPermissionsSection): ImmutableMap<RoomPermissionsItem, RoomMember.Role> {
    return when (section) {
        ChangeRoomPermissionsSection.RoomDetails -> persistentMapOf(
            RoomPermissionsItem.ROOM_NAME to RoomMember.Role.ADMIN,
            RoomPermissionsItem.ROOM_AVATAR to RoomMember.Role.MODERATOR,
            RoomPermissionsItem.ROOM_TOPIC to RoomMember.Role.USER,
        )
        ChangeRoomPermissionsSection.MessagesAndContent -> persistentMapOf(
            RoomPermissionsItem.SEND_EVENTS to RoomMember.Role.ADMIN,
            RoomPermissionsItem.REDACT_EVENTS to RoomMember.Role.MODERATOR,
        )
        ChangeRoomPermissionsSection.MembershipModeration -> persistentMapOf(
            RoomPermissionsItem.INVITE to RoomMember.Role.ADMIN,
            RoomPermissionsItem.KICK to RoomMember.Role.MODERATOR,
            RoomPermissionsItem.BAN to RoomMember.Role.USER,
        )
    }
}
