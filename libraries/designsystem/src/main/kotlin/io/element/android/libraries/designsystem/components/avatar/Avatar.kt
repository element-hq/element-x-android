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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.element.android.libraries.designsystem.AvatarGradientEnd
import io.element.android.libraries.designsystem.AvatarGradientStart
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.preview.debugPlaceholderAvatar
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
    AsyncImage(
        model = avatarData,
        onError = {
            Timber.e(it.result.throwable, "Error loading avatar $it\n${it.result}")
        },
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        placeholder = debugPlaceholderAvatar(),
        modifier = modifier
    )
}

@Composable
private fun InitialsAvatar(
    avatarData: AvatarData,
    modifier: Modifier = Modifier,
) {
    val initialsGradient = Brush.linearGradient(
        listOf(
            AvatarGradientStart,
            AvatarGradientEnd,
        ),
        start = Offset(0.0f, 100f),
        end = Offset(100f, 0f)
    )
    Box(
        modifier.background(brush = initialsGradient),
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = avatarData.initial,
            fontSize = (avatarData.size.dp / 2).value.sp,
            color = Color.White,
        )
    }
}

@Preview(group = PreviewGroup.Avatars)
@Composable
fun AvatarPreview(@PreviewParameter(AvatarDataProvider::class) avatarData: AvatarData) =
    ElementThemedPreview { Avatar(avatarData) }
