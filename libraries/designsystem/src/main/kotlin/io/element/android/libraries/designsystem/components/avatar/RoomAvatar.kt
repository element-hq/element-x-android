/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun RoomAvatar(
    avatarData: AvatarData,
    avatarType: AvatarType.Room,
    modifier: Modifier = Modifier,
    hideAvatarImage: Boolean = false,
    contentDescription: String? = null,
) {
    when {
        avatarType.isTombstoned -> {
            TombstonedRoomAvatar(
                size = avatarData.size,
                modifier = modifier,
                avatarType = avatarType,
                contentDescription = contentDescription
            )
        }
        avatarData.url != null || avatarType.heroes.isEmpty() -> {
            if (avatarData.url.isNullOrBlank() || hideAvatarImage) {
                InitialLetterAvatar(
                    avatarData = avatarData,
                    avatarType = avatarType,
                    modifier = modifier,
                    contentDescription = contentDescription,
                    forcedAvatarSize = null,
                )
            } else {
                ImageAvatar(
                    avatarData = avatarData,
                    avatarType = avatarType,
                    forcedAvatarSize = null,
                    modifier = modifier,
                    contentDescription = contentDescription,
                )
            }
        }
        else -> {
            AvatarCluster(
                avatars = avatarType.heroes,
                modifier = modifier,
                hideAvatarImages = hideAvatarImage,
                contentDescription = contentDescription
            )
        }
    }
}
