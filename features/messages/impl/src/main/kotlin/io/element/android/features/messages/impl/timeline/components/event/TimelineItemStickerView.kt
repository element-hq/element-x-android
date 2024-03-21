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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContentProvider
import io.element.android.libraries.designsystem.components.blurhash.BlurHashAsyncImage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.ui.media.MediaRequestData

private const val STICKER_SIZE_IN_DP = 128
private const val DEFAULT_ASPECT_RATIO = 1.33f

@Composable
fun TimelineItemStickerView(
    content: TimelineItemStickerContent,
    modifier: Modifier = Modifier,
) {
    val safeAspectRatio = content.aspectRatio ?: DEFAULT_ASPECT_RATIO
    Box(
        modifier = modifier
            .heightIn(min = STICKER_SIZE_IN_DP.dp, max = STICKER_SIZE_IN_DP.dp)
            .aspectRatio(safeAspectRatio, false),
        contentAlignment = Alignment.TopStart,
    ) {
        BlurHashAsyncImage(
            model = MediaRequestData(content.preferredMediaSource, MediaRequestData.Kind.File(content.body, content.mimeType)),
            blurHash = content.blurhash,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemStickerViewPreview(@PreviewParameter(TimelineItemStickerContentProvider::class) content: TimelineItemStickerContent) = ElementPreview {
    TimelineItemStickerView(content)
}
