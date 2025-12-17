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

@Composable
internal fun UserAvatar(
    avatarData: AvatarData,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    forcedAvatarSize: Dp? = null,
    hideImage: Boolean = false,
) {
    InitialOrImageAvatar(
        avatarData = avatarData,
        hideAvatarImage = hideImage,
        avatarShape = AvatarType.User.avatarShape(),
        modifier = modifier,
        contentDescription = contentDescription,
        forcedAvatarSize = forcedAvatarSize,
    )
}
