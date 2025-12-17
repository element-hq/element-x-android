/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar.internal

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.AvatarColors
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

@Composable
internal fun TombstonedRoomAvatar(
    size: Dp,
    avatarShape: Shape,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    TextAvatar(
        text = "!",
        size = size,
        colors = AvatarColors(
            background = ElementTheme.colors.bgSubtlePrimary,
            foreground = ElementTheme.colors.iconTertiary
        ),
        modifier = modifier,
        avatarShape = avatarShape,
        contentDescription = contentDescription,
    )
}

@Preview(group = PreviewGroup.Avatars)
@Composable
internal fun TombstonedRoomAvatarPreview() = ElementPreview {
    TombstonedRoomAvatar(
        size = 52.dp,
        avatarShape = CircleShape,
        contentDescription = null,
    )
}
