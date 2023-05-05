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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContentProvider
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.libraries.designsystem.components.blurhash.BlurHashAsyncImage
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import kotlin.math.min

@Composable
fun TimelineItemVideoView(
    content: TimelineItemVideoContent,
    modifier: Modifier = Modifier,
) {
    val maxHeight = min(300, content.height ?: Int.MAX_VALUE)
    Box(
        modifier = modifier
            .heightIn(max = maxHeight.dp)
            .aspectRatio(content.aspectRatio),
        contentAlignment = Alignment.Center,
    ) {
        BlurHashAsyncImage(
            blurHash = content.blurhash,
            model = MediaRequestData(content.thumbnailSource, MediaRequestData.Kind.Content),
            contentScale = ContentScale.Fit,
        )
        Image(
            painterResource(id = androidx.media3.ui.R.drawable.exo_ic_play_circle_filled),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )

    }
}

@Preview
@Composable
internal fun TimelineItemVideoViewLightPreview(@PreviewParameter(TimelineItemImageContentProvider::class) content: TimelineItemImageContent) =
    ElementPreviewLight { ContentToPreview(content) }

@Preview
@Composable
internal fun TimelineItemVideoViewDarkPreview(@PreviewParameter(TimelineItemImageContentProvider::class) content: TimelineItemImageContent) =
    ElementPreviewDark { ContentToPreview(content) }

@Composable
private fun ContentToPreview(content: TimelineItemImageContent) {
    TimelineItemImageView(content)
}
