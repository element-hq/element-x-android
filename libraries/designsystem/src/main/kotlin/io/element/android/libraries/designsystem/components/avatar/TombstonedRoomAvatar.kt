/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.AvatarColors
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

@Composable
fun TombstonedRoomAvatar(
    size: AvatarSize,
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
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape),
        contentDescription = contentDescription
    )
}

@Preview(group = PreviewGroup.Avatars)
@Composable
internal fun TombstonedRoomAvatarPreview() = ElementPreview {
    TombstonedRoomAvatar(
        size = AvatarSize.RoomListItem,
        contentDescription = null,
    )
}
