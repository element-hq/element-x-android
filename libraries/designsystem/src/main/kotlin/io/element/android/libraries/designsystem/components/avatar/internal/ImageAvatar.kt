/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar.internal

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import timber.log.Timber
import kotlin.io.encoding.Base64

@Composable
internal fun ImageAvatar(
    avatarData: AvatarData,
    avatarShape: Shape,
    forcedAvatarSize: Dp?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val size = forcedAvatarSize ?: avatarData.size.dp
    val thumbnailBytes = remember(avatarData.thumbnail) {
        avatarData.thumbnail?.let { runCatchingExceptions { Base64.decode(it) }.getOrNull() }
    }
    SubcomposeAsyncImage(
        model = avatarData,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(avatarShape)
    ) {
        val collectedState by painter.state.collectAsState()
        when (val state = collectedState) {
            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
            is AsyncImagePainter.State.Error -> {
                SideEffect {
                    Timber.e(
                        state.result.throwable,
                        "Error loading avatar $state\n${state.result}"
                    )
                }
                ThumbnailOrInitialLetterAvatar(
                    avatarData = avatarData,
                    thumbnailBytes = thumbnailBytes,
                    avatarShape = avatarShape,
                    forcedAvatarSize = forcedAvatarSize,
                    contentDescription = contentDescription,
                )
            }
            else -> ThumbnailOrInitialLetterAvatar(
                avatarData = avatarData,
                thumbnailBytes = thumbnailBytes,
                avatarShape = avatarShape,
                forcedAvatarSize = forcedAvatarSize,
                contentDescription = contentDescription,
            )
        }
    }
}

/**
 * Render the locally stored thumbnail of the avatar if any, else the initial letter.
 */
@Composable
private fun ThumbnailOrInitialLetterAvatar(
    avatarData: AvatarData,
    thumbnailBytes: ByteArray?,
    avatarShape: Shape,
    forcedAvatarSize: Dp?,
    contentDescription: String?,
) {
    if (thumbnailBytes == null) {
        InitialLetterAvatar(
            avatarData = avatarData,
            avatarShape = avatarShape,
            forcedAvatarSize = forcedAvatarSize,
            contentDescription = contentDescription,
        )
    } else {
        SubcomposeAsyncImage(
            model = thumbnailBytes,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(forcedAvatarSize ?: avatarData.size.dp)
                .clip(avatarShape)
        ) {
            val collectedState by painter.state.collectAsState()
            when (collectedState) {
                is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                else -> InitialLetterAvatar(
                    avatarData = avatarData,
                    avatarShape = avatarShape,
                    forcedAvatarSize = forcedAvatarSize,
                    contentDescription = contentDescription,
                )
            }
        }
    }
}
