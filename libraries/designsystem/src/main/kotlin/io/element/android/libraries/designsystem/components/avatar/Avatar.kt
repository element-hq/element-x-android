/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.colors.AvatarColorsProvider
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.preview.debugPlaceholderAvatar
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.Text
import timber.log.Timber

@Composable
fun Avatar(
    avatarData: AvatarData,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    // If not null, will be used instead of the size from avatarData
    forcedAvatarSize: Dp? = null,
) {
    val commonModifier = modifier
        .size(forcedAvatarSize ?: avatarData.size.dp)
        .clip(CircleShape)
    if (avatarData.url.isNullOrBlank()) {
        InitialsAvatar(
            avatarData = avatarData,
            forcedAvatarSize = forcedAvatarSize,
            modifier = commonModifier,
        )
    } else {
        ImageAvatar(
            avatarData = avatarData,
            forcedAvatarSize = forcedAvatarSize,
            modifier = commonModifier,
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun ImageAvatar(
    avatarData: AvatarData,
    forcedAvatarSize: Dp?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    if (LocalInspectionMode.current) {
        // For compose previews, use debugPlaceholderAvatar()
        // instead of falling back to initials avatar on load failure
        AsyncImage(
            model = avatarData,
            contentDescription = contentDescription,
            placeholder = debugPlaceholderAvatar(),
            modifier = modifier
        )
    } else {
        SubcomposeAsyncImage(
            model = avatarData,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        ) {
            when (val state = painter.state) {
                is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                is AsyncImagePainter.State.Error -> {
                    SideEffect {
                        Timber.e(state.result.throwable, "Error loading avatar $state\n${state.result}")
                    }
                    InitialsAvatar(
                        avatarData = avatarData,
                        forcedAvatarSize = forcedAvatarSize,
                    )
                }
                else -> InitialsAvatar(
                    avatarData = avatarData,
                    forcedAvatarSize = forcedAvatarSize,
                )
            }
        }
    }
}

@Composable
private fun InitialsAvatar(
    avatarData: AvatarData,
    forcedAvatarSize: Dp?,
    modifier: Modifier = Modifier,
) {
    val avatarColors = AvatarColorsProvider.provide(avatarData.id)
    Box(
        modifier.background(color = avatarColors.background)
    ) {
        val fontSize = (forcedAvatarSize ?: avatarData.size.dp).toSp() / 2
        val originalFont = ElementTheme.typography.fontHeadingMdBold
        val ratio = fontSize.value / originalFont.fontSize.value
        val lineHeight = originalFont.lineHeight * ratio
        Text(
            modifier = Modifier
                .clearAndSetSemantics {}
                .align(Alignment.Center),
            text = avatarData.initial,
            style = originalFont.copy(fontSize = fontSize, lineHeight = lineHeight, letterSpacing = 0.sp),
            color = avatarColors.foreground,
        )
    }
}

@Preview(group = PreviewGroup.Avatars)
@Composable
internal fun AvatarPreview(@PreviewParameter(AvatarDataProvider::class) avatarData: AvatarData) =
    ElementThemedPreview {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Avatar(avatarData)
            Text(text = avatarData.size.name + " " + avatarData.size.dp)
        }
    }
