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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContentProvider
import io.element.android.libraries.designsystem.components.BlurHashAsyncImage
import io.element.android.libraries.designsystem.modifiers.roundedBackground
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.wysiwyg.compose.EditorStyledText

@Composable
fun TimelineItemVideoView(
    content: TimelineItemVideoContent,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(CommonStrings.common_image)
    Column()
    {
        TimelineItemAspectRatioBox(
            aspectRatio = content.aspectRatio,
            modifier = modifier.semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            BlurHashAsyncImage(
                model = MediaRequestData(content.thumbnailSource, MediaRequestData.Kind.File(content.body, content.mimeType)),
                blurHash = content.blurHash,
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier.roundedBackground(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    Icons.Default.PlayArrow,
                    contentDescription = stringResource(id = CommonStrings.a11y_play),
                    colorFilter = ColorFilter.tint(Color.White),
                )
            }
        }

        if (content.showCaption) {
            Spacer(modifier = Modifier.height(8.dp))
            Box ( )
            {
                EditorStyledText(
                    modifier = Modifier
                        .widthIn(min = MIN_HEIGHT_IN_DP.dp * content.aspectRatio!!, max = MAX_HEIGHT_IN_DP.dp * content.aspectRatio),
                    text = content.caption,
                    style = ElementRichTextEditorStyle.textStyle(),
                    releaseOnDetach = false
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemVideoViewPreview(@PreviewParameter(TimelineItemVideoContentProvider::class) content: TimelineItemVideoContent) = ElementPreview {
    TimelineItemVideoView(content)
}
