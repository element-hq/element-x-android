/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import android.text.SpannedString
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.components.ATimelineItemEventRow
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContentProvider
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.protection.ProtectedView
import io.element.android.libraries.designsystem.components.blurhash.blurHashBackground
import io.element.android.libraries.designsystem.modifiers.roundedBackground
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.wysiwyg.compose.EditorStyledText

@Composable
fun TimelineItemVideoView(
    content: TimelineItemVideoContent,
    hideMediaContent: Boolean,
    onShowClick: () -> Unit,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(CommonStrings.common_image)
    Column(
        modifier = modifier.semantics { contentDescription = description }
    ) {
        val containerModifier = if (content.showCaption) {
            Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
        } else {
            Modifier
        }
        TimelineItemAspectRatioBox(
            modifier = containerModifier.blurHashBackground(content.blurHash, alpha = 0.9f),
            aspectRatio = content.aspectRatio,
            contentAlignment = Alignment.Center,
        ) {
            ProtectedView(
                hideContent = hideMediaContent,
                onShowClick = onShowClick,
            ) {
                var isLoaded by remember { mutableStateOf(false) }
                AsyncImage(
                    modifier = Modifier
                            .fillMaxWidth()
                            .then(if (isLoaded) Modifier.background(Color.White) else Modifier),
                    model = MediaRequestData(
                        source = content.thumbnailSource,
                        kind = MediaRequestData.Kind.File(
                            fileName = content.filename,
                            mimeType = content.mimeType
                        )
                    ),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center,
                    contentDescription = description,
                    onState = { isLoaded = it is AsyncImagePainter.State.Success },
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
        }

        if (content.showCaption) {
            Spacer(modifier = Modifier.height(8.dp))
            val caption = if (LocalInspectionMode.current) {
                SpannedString(content.caption)
            } else {
                content.formattedCaption?.body
                    ?.takeIf { content.formattedCaption.format == MessageFormat.HTML }
                    ?: SpannedString(content.caption)
            }
            CompositionLocalProvider(
                LocalContentColor provides ElementTheme.colors.textPrimary,
                LocalTextStyle provides ElementTheme.typography.fontBodyLgRegular,
            ) {
                val aspectRatio = content.aspectRatio ?: DEFAULT_ASPECT_RATIO
                EditorStyledText(
                    modifier = Modifier
                        .widthIn(min = MIN_HEIGHT_IN_DP.dp * aspectRatio, max = MAX_HEIGHT_IN_DP.dp * aspectRatio),
                    text = caption,
                    style = ElementRichTextEditorStyle.textStyle(),
                    releaseOnDetach = false,
                    onTextLayout = ContentAvoidingLayout.measureLegacyLastTextLine(onContentLayoutChange = onContentLayoutChange),
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemVideoViewPreview(@PreviewParameter(TimelineItemVideoContentProvider::class) content: TimelineItemVideoContent) = ElementPreview {
    TimelineItemVideoView(
        content = content,
        hideMediaContent = false,
        onShowClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemVideoViewHideMediaContentPreview() = ElementPreview {
    TimelineItemVideoView(
        content = aTimelineItemVideoContent(),
        hideMediaContent = true,
        onShowClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineVideoWithCaptionRowPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = isMine,
                    content = aTimelineItemVideoContent().copy(
                        filename = "video.mp4",
                        caption = "A long caption that may wrap into several lines",
                        aspectRatio = 2.5f,
                    ),
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = false,
                content = aTimelineItemVideoContent().copy(
                    filename = "video.mp4",
                    caption = "Video with null aspect ratio",
                    aspectRatio = null,
                ),
                groupPosition = TimelineItemGroupPosition.Last,
            ),
        )
    }
}
