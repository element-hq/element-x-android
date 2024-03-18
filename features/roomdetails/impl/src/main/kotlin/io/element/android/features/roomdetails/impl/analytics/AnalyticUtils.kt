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

package io.element.android.features.roomdetails.impl.analytics

import im.vector.app.features.analytics.plan.RoomModeration
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import io.element.android.services.analytics.api.AnalyticsService

internal fun RoomMember.Role.toAnalyticsMemberRole(): RoomModeration.Role = when (this) {
    RoomMember.Role.ADMIN -> RoomModeration.Role.Administrator
    RoomMember.Role.MODERATOR -> RoomModeration.Role.Moderator
    RoomMember.Role.USER -> RoomModeration.Role.User
}

internal fun analyticsMemberRoleForPowerLevel(powerLevel: Long): RoomModeration.Role {
    return RoomMember.Role.forPowerLevel(powerLevel).toAnalyticsMemberRole()
}

internal fun AnalyticsService.trackPermissionChangeAnalytics(initial: MatrixRoomPowerLevels?, updated: MatrixRoomPowerLevels) {
    if (updated.ban != initial?.ban) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsBanMembers, analyticsMemberRoleForPowerLevel(updated.ban)))
    }
    if (updated.invite != initial?.invite) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsInviteUsers, analyticsMemberRoleForPowerLevel(updated.invite)))
    }
    if (updated.kick != initial?.kick) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsKickMembers, analyticsMemberRoleForPowerLevel(updated.kick)))
    }
    if (updated.sendEvents != initial?.sendEvents) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsSendMessages, analyticsMemberRoleForPowerLevel(updated.sendEvents)))
    }
    if (updated.redactEvents != initial?.redactEvents) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsRedactMessages, analyticsMemberRoleForPowerLevel(updated.redactEvents)))
    }
    if (updated.roomName != initial?.roomName) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsRoomName, analyticsMemberRoleForPowerLevel(updated.roomName)))
    }
    if (updated.roomAvatar != initial?.roomAvatar) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsRoomAvatar, analyticsMemberRoleForPowerLevel(updated.roomAvatar)))
    }
    if (updated.roomTopic != initial?.roomTopic) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsRoomTopic, analyticsMemberRoleForPowerLevel(updated.roomTopic)))
    }
}
