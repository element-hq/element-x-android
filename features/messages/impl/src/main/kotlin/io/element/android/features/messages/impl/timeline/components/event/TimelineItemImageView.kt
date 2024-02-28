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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContentProvider
import io.element.android.libraries.designsystem.components.BlurHashAsyncImage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun TimelineItemImageView(
    content: TimelineItemImageContent,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(CommonStrings.common_image)
    TimelineItemAspectRatioBox(
        aspectRatio = content.aspectRatio,
        modifier = modifier.semantics { contentDescription = description },
    ) {
        BlurHashAsyncImage(
            model = MediaRequestData(content.preferredMediaSource, MediaRequestData.Kind.File(content.body, content.mimeType)),
            blurHash = content.blurhash,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemImageViewPreview(@PreviewParameter(TimelineItemImageContentProvider::class) content: TimelineItemImageContent) = ElementPreview {
    TimelineItemImageView(content)
}
