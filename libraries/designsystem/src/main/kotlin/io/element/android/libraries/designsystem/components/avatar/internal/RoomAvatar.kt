/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.avatar.avatarShape
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun RoomAvatar(
    avatarData: AvatarData,
    avatarType: AvatarType.Room,
    modifier: Modifier = Modifier,
    hideAvatarImage: Boolean = false,
    forcedAvatarSize: Dp? = null,
    contentDescription: String? = null,
) {
    when {
        avatarType.isTombstoned -> {
            TombstonedRoomAvatar(
                size = forcedAvatarSize ?: avatarData.size.dp,
                modifier = modifier,
                avatarShape = avatarType.avatarShape(),
                contentDescription = contentDescription
            )
        }
        avatarData.url != null || avatarType.heroes.isEmpty() -> {
            InitialOrImageAvatar(
                avatarData = avatarData,
                hideAvatarImage = hideAvatarImage,
                avatarShape = avatarType.avatarShape(),
                forcedAvatarSize = forcedAvatarSize,
                modifier = modifier,
                contentDescription = contentDescription,
            )
        }
        else -> {
            AvatarCluster(
                // Keep only the first hero for now
                avatars = avatarType.heroes.take(1).toImmutableList(),
                // Note: even for a room avatar, we use AvatarType.User here to display the avatar of heroes
                avatarType = AvatarType.User,
                modifier = modifier,
                hideAvatarImages = hideAvatarImage,
                contentDescription = contentDescription
            )
        }
    }
}
