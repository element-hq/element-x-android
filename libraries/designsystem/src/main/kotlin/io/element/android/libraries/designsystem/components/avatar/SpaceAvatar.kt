/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.utils.CommonDrawables

@Composable
fun SpaceAvatar(
    avatarData: AvatarData,
    avatarType: AvatarType.Space,
    modifier: Modifier = Modifier,
    hideAvatarImage: Boolean = false,
    contentDescription: String? = null,
) {
    when {
        avatarType.isTombstoned -> TombstonedRoomAvatar(
            size = avatarData.size,
            avatarType = avatarType,
            modifier = modifier,
            contentDescription = contentDescription,
        )
        avatarData.url.isNullOrBlank() || hideAvatarImage -> InitialLetterAvatar(
            avatarData = avatarData,
            avatarType = avatarType,
            modifier = modifier,
            contentDescription = contentDescription,
            forcedAvatarSize = null,
        )
        else -> ImageAvatar(
            avatarData = avatarData,
            avatarType = avatarType,
            forcedAvatarSize = null,
            modifier = modifier,
            contentDescription = contentDescription,
        )
    }
}

@Preview(group = PreviewGroup.Avatars)
@Composable
internal fun SpaceAvatarPreview() =
    ElementThemedPreview(
        drawableFallbackForImages = CommonDrawables.sample_avatar,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SpaceAvatar(
                avatarData = anAvatarData(),
                avatarType = AvatarType.Space(cornerSize = 16.dp),
            )
            SpaceAvatar(
                avatarData = anAvatarData(),
                avatarType = AvatarType.Space(
                    cornerSize = 16.dp,
                    isTombstoned = true,
                ),
            )
        }
    }
