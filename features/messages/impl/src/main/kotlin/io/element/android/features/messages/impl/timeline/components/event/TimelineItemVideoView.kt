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

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContentProvider
import io.element.android.libraries.designsystem.components.BlurHashAsyncImage
import io.element.android.libraries.designsystem.modifiers.roundedBackground
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.matrix.ui.media.MediaRequestData

@Composable
fun TimelineItemVideoView(
    content: TimelineItemVideoContent,
    modifier: Modifier = Modifier,
) {
    TimelineItemAspectRatioBox(
        height = content.height,
        aspectRatio = content.aspectRatio,
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        BlurHashAsyncImage(
            model = MediaRequestData(content.thumbnailSource, MediaRequestData.Kind.Content),
            blurHash = content.blurHash,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
        Box(
            modifier = Modifier.roundedBackground(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                Icons.Default.PlayArrow,
                contentDescription = "Play",
                colorFilter = ColorFilter.tint(Color.White),
            )
        }
    }
}

@Preview
@Composable
internal fun TimelineItemVideoViewLightPreview(@PreviewParameter(TimelineItemVideoContentProvider::class) content: TimelineItemVideoContent) =
    ElementPreviewLight { ContentToPreview(content) }

@Preview
@Composable
internal fun TimelineItemVideoViewDarkPreview(@PreviewParameter(TimelineItemVideoContentProvider::class) content: TimelineItemVideoContent) =
    ElementPreviewDark { ContentToPreview(content) }

@Composable
private fun ContentToPreview(content: TimelineItemVideoContent) {
    TimelineItemVideoView(content)
}
