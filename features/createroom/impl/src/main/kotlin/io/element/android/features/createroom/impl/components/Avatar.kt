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

package io.element.android.features.createroom.impl.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.debugPlaceholderBackground
import io.element.android.libraries.designsystem.theme.LocalColors
import io.element.android.libraries.designsystem.theme.components.Icon

@Composable
fun Avatar(
    avatarUri: Uri?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val commonModifier = modifier
        .size(70.dp)
        .clip(CircleShape)
        .clickable(onClick = onClick)

    if (avatarUri != null) {
        val context = LocalContext.current
        val model = ImageRequest.Builder(context)
            .data(avatarUri)
            .build()
        AsyncImage(
            modifier = commonModifier,
            model = model,
            placeholder = debugPlaceholderBackground(ColorPainter(MaterialTheme.colorScheme.surfaceVariant)),
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )
    } else {
        Box(modifier = commonModifier.background(LocalColors.current.quinary)) {
            Icon(
                imageVector = Icons.Outlined.AddAPhoto,
                contentDescription = "",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Preview
@Composable
fun AvatarLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
fun AvatarDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Row {
        Avatar(null)
        Avatar(Uri.EMPTY)
    }
}
