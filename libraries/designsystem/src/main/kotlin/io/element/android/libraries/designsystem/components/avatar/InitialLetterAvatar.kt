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
import io.element.android.libraries.designsystem.colors.AvatarColorsProvider

@Composable
internal fun InitialLetterAvatar(
    avatarData: AvatarData,
    avatarType: AvatarType,
    forcedAvatarSize: Dp?,
    contentDescription: String?,
    modifier: Modifier = Modifier.Companion,
) {
    val avatarColors = AvatarColorsProvider.provide(avatarData.id)
    TextAvatar(
        text = avatarData.initialLetter,
        size = forcedAvatarSize ?: avatarData.size.dp,
        avatarType = avatarType,
        colors = avatarColors,
        contentDescription = contentDescription,
        modifier = modifier
    )
}
