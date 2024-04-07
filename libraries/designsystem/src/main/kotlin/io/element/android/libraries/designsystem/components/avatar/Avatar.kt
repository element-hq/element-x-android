/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.colors.AvatarColorsProvider
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.Text
import timber.log.Timber

@Composable
fun Avatar(
    avatarData: AvatarData,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val commonModifier = modifier
        .size(avatarData.size.dp)
        .clip(CircleShape)
    if (avatarData.url.isNullOrBlank()) {
        InitialsAvatar(
            avatarData = avatarData,
            modifier = commonModifier,
        )
    } else {
        ImageAvatar(
            avatarData = avatarData,
            modifier = commonModifier,
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun ImageAvatar(
    avatarData: AvatarData,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
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
                InitialsAvatar(avatarData = avatarData)
            }
            else -> InitialsAvatar(avatarData = avatarData)
        }
    }
}

@Composable
private fun InitialsAvatar(
    avatarData: AvatarData,
    modifier: Modifier = Modifier,
) {
    val avatarColors = AvatarColorsProvider.provide(avatarData.id, ElementTheme.isLightTheme)
    Box(
        modifier.background(color = avatarColors.background)
    ) {
        val fontSize = avatarData.size.dp.toSp() / 2
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
