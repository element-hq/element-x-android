/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import timber.log.Timber

@Composable
internal fun ImageAvatar(
    avatarData: AvatarData,
    avatarType: AvatarType,
    forcedAvatarSize: Dp?,
    modifier: Modifier = Modifier.Companion,
    contentDescription: String? = null,
) {
    val size = forcedAvatarSize ?: avatarData.size.dp
    SubcomposeAsyncImage(
        model = avatarData,
        contentDescription = contentDescription,
        contentScale = ContentScale.Companion.Crop,
        modifier = modifier
                .size(size)
                .clip(avatarType.avatarShape())
    ) {
        val collectedState by painter.state.collectAsState()
        when (val state = collectedState) {
            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
            is AsyncImagePainter.State.Error -> {
                SideEffect {
                    Timber.Forest.e(
                        state.result.throwable,
                        "Error loading avatar $state\n${state.result}"
                    )
                }
                InitialLetterAvatar(
                    avatarData = avatarData,
                    avatarType = avatarType,
                    forcedAvatarSize = forcedAvatarSize,
                    contentDescription = contentDescription,
                )
            }
            else -> InitialLetterAvatar(
                avatarData = avatarData,
                avatarType = avatarType,
                forcedAvatarSize = forcedAvatarSize,
                contentDescription = contentDescription,
            )
        }
    }
}
