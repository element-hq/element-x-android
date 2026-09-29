/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.model.event.AttachmentItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAttachmentsContent
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeAudio
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationState
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import io.element.android.libraries.matrix.ui.media.contentvalidation.InvalidContentView
import io.element.android.libraries.matrix.ui.media.contentvalidation.NotFoundContentView
import io.element.android.libraries.matrix.ui.media.contentvalidation.collectMediaState
import io.element.android.libraries.matrix.ui.media.contentvalidation.rememberEventContentValidationState
import io.element.android.libraries.ui.strings.CommonStrings
import kotlin.math.max

@Composable
fun TimelineItemAttachmentsListView(
    eventId: EventId?,
    content: TimelineItemAttachmentsContent,
    onGalleryItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentValidationState = rememberEventContentValidationState(
        eventId = eventId,
        needsValidation = content.isMedia,
    )

    Column(modifier = modifier.fillMaxWidth()) {
        val validationStates = remember(content.attachments) {
            buildList {
                for (attachment in content.attachments) {
                    add(validationStateForAttachment(attachment, contentValidationState))
                }
            }.toMutableStateList()
        }
        content.attachments.forEachIndexed { index, attachment ->
            Column(modifier = Modifier.fillMaxWidth()) {
                val thumbnailContentValidationState by contentValidationState.collectMediaState(attachment.thumbnailSource?.safeUrl)
                val mediaContentValidationState by contentValidationState.collectMediaState(attachment.mediaSource.safeUrl)

                val itemContentValidationState = remember(thumbnailContentValidationState, mediaContentValidationState) {
                    validationStateForAttachment(attachment, contentValidationState)
                }

                LaunchedEffect(itemContentValidationState) {
                    validationStates[index] = itemContentValidationState
                }

                val needsSeparator = index in 1..max(1, content.attachments.lastIndex)
                val isPreviousItemInvalid = validationStates.getOrNull(index - 1)?.hasError() == true
                if (needsSeparator && !itemContentValidationState.hasError() && !isPreviousItemInvalid) {
                    HorizontalDivider(
                        color = ElementTheme.colors.borderInteractiveSecondary,
                    )
                }

                when (itemContentValidationState) {
                    ContentValidationValue.Invalid -> {
                        val shape = RoundedCornerShape(6.dp)
                        InvalidContentView(
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                                .border(width = 1.dp, color = ElementTheme.colors.borderCriticalSubtle, shape = shape)
                                .clip(shape),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                    is ContentValidationValue.UnrecoverableError -> {
                        val shape = RoundedCornerShape(6.dp)
                        NotFoundContentView(
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                                .border(width = 1.dp, color = ElementTheme.colors.borderCriticalSubtle, shape = shape)
                                .clip(shape),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                    ContentValidationValue.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally))
                    }
                    ContentValidationValue.Valid -> {
                        AttachmentListItem(
                            attachment = attachment,
                            onClick = { onGalleryItemClick(index) },
                        )
                    }
                    else -> {
                        // TODO handle cases where either the content hasn't started validation or it failed to do so
                    }
                }

                if (index == content.attachments.lastIndex && content.showCaption && !itemContentValidationState.isInvalid()) {
                    HorizontalDivider(
                        color = ElementTheme.colors.borderInteractiveSecondary,
                    )
                }
            }
        }
    }
}

private fun validationStateForAttachment(
    attachment: AttachmentItem,
    contentValidationState: ContentValidationState,
): ContentValidationValue {
    val thumbnailValue = attachment.thumbnailSource?.safeUrl?.let { contentValidationState.getCurrentMediaState(it) } ?: ContentValidationValue.Unknown
    val mediaValue = contentValidationState.getCurrentMediaState(attachment.mediaSource.safeUrl)
    return validationStateForAttachment(thumbnailValue, mediaValue)
}

private fun validationStateForAttachment(
    thumbnailValue: ContentValidationValue,
    mediaValue: ContentValidationValue,
): ContentValidationValue {
    return if (thumbnailValue.isInvalid() || mediaValue.isInvalid()) {
        ContentValidationValue.Invalid
    } else if (thumbnailValue.hasUnrecoverableError() || mediaValue.hasUnrecoverableError()) {
        listOf(thumbnailValue, mediaValue).first { it.hasUnrecoverableError() }
    } else if (thumbnailValue.isLoading() || mediaValue.isLoading()) {
        ContentValidationValue.Loading
    } else {
        mediaValue
    }
}

@Composable
private fun AttachmentListItem(
    attachment: AttachmentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconSize = 36.dp
    val thumbnailSize = 36L
    val spacing = 8.dp
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(RoundedCornerShape(4.dp))
                .background(ElementTheme.colors.bgCanvasDefault),
            contentAlignment = Alignment.Center,
        ) {
            if (attachment.thumbnailSource != null) {
                val isVideo = attachment.mimeType.isMimeTypeVideo()
                AsyncImage(
                    model = MediaRequestData(
                        source = attachment.thumbnailSource,
                        kind = MediaRequestData.Kind.Thumbnail(thumbnailSize),
                    ),
                    contentDescription = attachment.filename,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(4.dp)),
                )
                if (isVideo) {
                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .background(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = CompoundIcons.VideoCallSolid(),
                            contentDescription = stringResource(CommonStrings.common_video),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            } else {
                val isImage = attachment.mimeType.isMimeTypeImage()
                val isVideo = attachment.mimeType.isMimeTypeVideo()
                val isAudio = attachment.mimeType.isMimeTypeAudio()
                val icon = when {
                    isImage -> CompoundIcons.Image()
                    isVideo -> CompoundIcons.VideoCall()
                    isAudio -> CompoundIcons.Audio()
                    else -> CompoundIcons.Attachment()
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElementTheme.colors.iconPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = attachment.filename,
                color = ElementTheme.colors.textPrimary,
                style = ElementTheme.typography.fontBodyLgRegular,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${attachment.fileExtension} • ${attachment.formattedFileSize}",
                color = ElementTheme.colors.textSecondary,
                style = ElementTheme.typography.fontBodySmRegular,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemAttachmentsListViewPreview(
    @PreviewParameter(TimelineItemAttachmentsContentPreviewParam::class) content: TimelineItemAttachmentsContent
) = ElementPreview {
    TimelineItemAttachmentsListView(
        eventId = EventId("\$eventId"),
        content = content,
        onGalleryItemClick = {},
    )
}
