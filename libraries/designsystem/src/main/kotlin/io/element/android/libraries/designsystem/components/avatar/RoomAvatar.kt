/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList

@Composable
fun RoomAvatar(
    avatarData: AvatarData,
    heroes: ImmutableList<AvatarData>,
    modifier: Modifier = Modifier,
    isTombstoned: Boolean = false,
    hideAvatarImage: Boolean = false,
    contentDescription: String? = null,
) {
    when {
        isTombstoned -> {
            TombstonedRoomAvatar(
                size = avatarData.size,
                modifier = modifier,
                contentDescription = contentDescription
            )
        }
        avatarData.url != null || heroes.isEmpty() -> {
            Avatar(
                avatarData = avatarData,
                modifier = modifier,
                contentDescription = contentDescription,
                hideImage = hideAvatarImage
            )
        }
        else -> {
            AvatarCluster(
                avatars = heroes,
                modifier = modifier,
                hideAvatarImages = hideAvatarImage,
                contentDescription = contentDescription
            )
        }
    }
}
