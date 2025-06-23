/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.compound.theme.AvatarColors
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

@Composable
internal fun TombstonedRoomAvatar(
    size: AvatarSize,
    avatarType: AvatarType,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    TextAvatar(
        text = "!",
        size = size.dp,
        colors = AvatarColors(
            background = ElementTheme.colors.bgSubtlePrimary,
            foreground = ElementTheme.colors.iconTertiary
        ),
        modifier = modifier,
        avatarType = avatarType,
        contentDescription = contentDescription,
    )
}

@Preview(group = PreviewGroup.Avatars)
@Composable
internal fun TombstonedRoomAvatarPreview() = ElementPreview {
    TombstonedRoomAvatar(
        size = AvatarSize.RoomListItem,
        avatarType = AvatarType.Room(),
        contentDescription = null,
    )
}
