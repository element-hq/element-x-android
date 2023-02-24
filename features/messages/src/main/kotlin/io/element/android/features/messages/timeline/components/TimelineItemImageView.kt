/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.features.messages.timeline.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.element.android.features.messages.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.timeline.model.event.TimelineItemImageContentProvider
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.debugPlaceholderBackground

@Composable
fun TimelineItemImageView(
    content: TimelineItemImageContent,
    modifier: Modifier = Modifier
) {
    val widthPercent = if (content.aspectRatio > 1f) {
        1f
    } else {
        0.7f
    }
    Box(
        modifier = modifier
            .fillMaxWidth(widthPercent)
            .aspectRatio(content.aspectRatio),
        contentAlignment = Alignment.Center,
    ) {
        val isLoading = rememberSaveable(content.imageMeta) { mutableStateOf(true) }
        val context = LocalContext.current
        val model = ImageRequest.Builder(context)
            .data(content.imageMeta)
            .build()

        AsyncImage(
            model = model,
            contentDescription = null,
            placeholder = debugPlaceholderBackground(ColorPainter(MaterialTheme.colorScheme.surfaceVariant)),
            contentScale = ContentScale.Crop,
            onSuccess = { isLoading.value = false },
        )
    }
}

@Preview
@Composable
internal fun TimelineItemImageViewLightPreview(@PreviewParameter(TimelineItemImageContentProvider::class) content: TimelineItemImageContent) =
    ElementPreviewLight { ContentToPreview(content) }

@Preview
@Composable
internal fun TimelineItemImageViewDarkPreview(@PreviewParameter(TimelineItemImageContentProvider::class) content: TimelineItemImageContent) =
    ElementPreviewDark { ContentToPreview(content) }

@Composable
private fun ContentToPreview(content: TimelineItemImageContent) {
    TimelineItemImageView(content)
}
