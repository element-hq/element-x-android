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

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

data class ChangeRoomPermissionsState(
    val section: ChangeRoomPermissionsSection,
    val currentPermissions: ImmutableMap<RoomPermissionsItem, RoomMember.Role>,
    val items: ImmutableList<RoomPermissionsItem>,
    val hasChanges: Boolean,
    val saveAction: AsyncAction<Unit>,
    val confirmExitAction: AsyncAction<Unit>,
    val eventSink: (ChangeRoomPermissionsEvent) -> Unit,
)

enum class RoomPermissionsItem {
    BAN,
    INVITE,
    KICK,
    SEND_EVENTS,
    REDACT_EVENTS,
    ROOM_NAME,
    ROOM_AVATAR,
    ROOM_TOPIC
}
