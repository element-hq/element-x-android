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
internal fun InitialOrImageAvatar(
    avatarData: AvatarData,
    hideAvatarImage: Boolean,
    forcedAvatarSize: Dp?,
    avatarType: AvatarType,
    modifier: Modifier,
    contentDescription: String?
) {
    when {
        avatarData.url.isNullOrBlank() || hideAvatarImage -> InitialLetterAvatar(
            avatarData = avatarData,
            avatarType = avatarType,
            forcedAvatarSize = forcedAvatarSize,
            modifier = modifier,
            contentDescription = contentDescription,
        )
        else -> ImageAvatar(
            avatarData = avatarData,
            avatarType = avatarType,
            forcedAvatarSize = forcedAvatarSize,
            modifier = modifier,
            contentDescription = contentDescription,
        )
    }
}
