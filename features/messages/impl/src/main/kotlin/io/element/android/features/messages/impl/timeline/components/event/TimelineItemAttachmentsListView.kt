/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.AttachmentItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAttachmentsContent
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeAudio
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun TimelineItemAttachmentsListView(
    content: TimelineItemAttachmentsContent,
    onContentClick: ((Int) -> Unit)?,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (content.attachments.isEmpty()) return

    Column(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            content.attachments.forEachIndexed { index, attachment ->
                Column {
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier,
                            thickness = 1.dp,
                        )
                    }
                    AttachmentListItem(
                        attachment = attachment,
                        onClick = onContentClick?.let { { it(index) } },
                        onContentLayoutChange = onContentLayoutChange,
                    )
                }
            }
        }

        if (content.showCaption) {
            HorizontalDivider(
                modifier = Modifier,
                thickness = 1.dp,
            )
            val caption = if (LocalInspectionMode.current) {
                android.text.SpannedString(content.caption)
            } else {
                (content.formattedCaption ?: android.text.SpannedString(content.caption)).let {
                    if (it is String) it else android.text.SpannedString(content.caption)
                }
            }
            CompositionLocalProvider(
                LocalContentColor provides ElementTheme.colors.textPrimary,
                LocalTextStyle provides ElementTheme.typography.fontBodyLgRegular
            ) {
                Text(
                    modifier = Modifier
                        .padding(top = 8.dp, start = 4.dp, end = 4.dp)
                        .widthIn(min = 120.dp),
                    text = caption.toString(),
                    style = ElementTheme.typography.fontBodyLgRegular,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AttachmentListItem(
    attachment: AttachmentItem,
    onClick: (() -> Unit)?,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconSize = 36.dp
    val thumbnailSize = 36L
    val spacing = 8.dp
    val hasCaption = false

    Row(
        modifier = modifier
            .padding(vertical = 6.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
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
                                color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = CompoundIcons.VideoCallSolid(),
                            contentDescription = stringResource(CommonStrings.common_video),
                            tint = androidx.compose.ui.graphics.Color.White,
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
                onTextLayout = if (hasCaption) {
                    {}
                } else {
                    ContentAvoidingLayout.measureLastTextLine(
                        onContentLayoutChange = onContentLayoutChange,
                        extraWidth = iconSize + spacing
                    )
                },
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemAttachmentsListViewPreview() = ElementPreview {
    TimelineItemAttachmentsListView(
        content = TimelineItemAttachmentsContent(
            body = "Files",
            caption = null,
            formattedCaption = null,
            isEdited = false,
            attachments = listOf(
                AttachmentItem(
                    filename = "document.pdf",
                    mimeType = "application/pdf",
                    mediaSource = io.element.android.libraries.matrix.api.media.MediaSource(url = "", json = ""),
                    thumbnailSource = null,
                    fileSize = null,
                    formattedFileSize = "2.5 MB",
                    fileExtension = "PDF",
                ),
                AttachmentItem(
                    filename = "photo.jpg",
                    mimeType = "image/jpeg",
                    mediaSource = io.element.android.libraries.matrix.api.media.MediaSource(url = "", json = ""),
                    thumbnailSource = io.element.android.libraries.matrix.api.media.MediaSource(url = "thumb", json = ""),
                    fileSize = null,
                    formattedFileSize = "1.2 MB",
                    fileExtension = "JPG",
                ),
                AttachmentItem(
                    filename = "spreadsheet.xlsx",
                    mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    mediaSource = io.element.android.libraries.matrix.api.media.MediaSource(url = "", json = ""),
                    thumbnailSource = null,
                    fileSize = null,
                    formattedFileSize = "450 KB",
                    fileExtension = "XLSX",
                ),
            ),
        ),
        onContentClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemAttachmentsListViewWithCaptionPreview() = ElementPreview {
    TimelineItemAttachmentsListView(
        content = TimelineItemAttachmentsContent(
            body = "Files",
            caption = "Important documents",
            formattedCaption = null,
            isEdited = false,
            attachments = listOf(
                AttachmentItem(
                    filename = "report.pdf",
                    mimeType = "application/pdf",
                    mediaSource = io.element.android.libraries.matrix.api.media.MediaSource(url = "", json = ""),
                    thumbnailSource = null,
                    fileSize = null,
                    formattedFileSize = "3.2 MB",
                    fileExtension = "PDF",
                ),
                AttachmentItem(
                    filename = "notes.txt",
                    mimeType = "text/plain",
                    mediaSource = io.element.android.libraries.matrix.api.media.MediaSource(url = "", json = ""),
                    thumbnailSource = null,
                    fileSize = null,
                    formattedFileSize = "12 KB",
                    fileExtension = "TXT",
                ),
            ),
        ),
        onContentClick = {},
        onContentLayoutChange = {},
    )
}