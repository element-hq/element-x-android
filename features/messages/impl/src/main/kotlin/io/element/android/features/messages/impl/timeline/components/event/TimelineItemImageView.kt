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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContentProvider
import io.element.android.libraries.designsystem.components.BlurHashAsyncImage
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.matrix.ui.media.MediaRequestData

@Composable
fun TimelineItemImageView(
    content: TimelineItemImageContent,
    modifier: Modifier = Modifier,
) {
    TimelineItemAspectRatioBox(
        aspectRatio = content.aspectRatio,
        modifier = modifier
    ) {
        BlurHashAsyncImage(
            model = MediaRequestData(content.preferredMediaSource, MediaRequestData.Kind.File(content.body, content.mimeType)),
            blurHash = content.blurhash,
            contentScale = ContentScale.Crop,
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
