/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.model

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMember

fun RoomInfo.getAvatarData(size: AvatarSize) = AvatarData(
    id = id.value,
    name = name,
    url = avatarUrl,
    size = size,
)

/**
 * Returns the role of the user in the room.
 * If the user is a creator and [RoomInfo.privilegedCreatorRole] is true, returns [RoomMember.Role.Owner].
 * Otherwise, checks the power levels and returns the corresponding role.
 * If no specific power level is set for the user, defaults to [RoomMember.Role.User].
 */
fun RoomInfo.roleOf(userId: UserId): RoomMember.Role {
    return if (privilegedCreatorRole && creators.contains(userId)) {
        RoomMember.Role.Owner(isCreator = true)
    } else {
        roomPowerLevels?.roleOf(userId) ?: RoomMember.Role.User
    }
}
