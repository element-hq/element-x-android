/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
internal fun UserAvatar(
    avatarData: AvatarData,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    forcedAvatarSize: Dp? = null,
    hideImage: Boolean = false,
) {
    if (avatarData.url.isNullOrBlank() || hideImage) {
        InitialLetterAvatar(
            avatarData = avatarData,
            avatarType = AvatarType.User,
            forcedAvatarSize = forcedAvatarSize,
            modifier = modifier,
            contentDescription = contentDescription,
        )
    } else {
        ImageAvatar(
            avatarData = avatarData,
            avatarType = AvatarType.User,
            forcedAvatarSize = forcedAvatarSize,
            modifier = modifier,
            contentDescription = contentDescription,
        )
    }
}
