/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.urlpreview.UrlPreviewData
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.wysiwyg.link.Link

@Composable
fun TimelineItemUrlPreviewView(
    preview: UrlPreviewData,
    onClick: (Link) -> Unit,
    onLongClick: (Link) -> Unit,
    cardWidth: Dp? = null,
    modifier: Modifier = Modifier,
) {
    val link = Link(preview.url, preview.title ?: preview.url)
    Column(
        modifier = modifier
            .then(
                if (cardWidth != null) {
                    Modifier.width(cardWidth)
                } else {
                    Modifier.widthIn(max = 296.dp)
                }
            )
            .clip(RoundedCornerShape(12.dp))
            .background(ElementTheme.colors.bgSubtleSecondary)
            .combinedClickable(
                onClick = { onClick(link) },
                onLongClick = { onLongClick(link) },
            )
            .semantics(mergeDescendants = true) {}
    ) {
        preview.imageUrl?.let { imageUrl ->
            previewImageModel(imageUrl)?.let { imageModel ->
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp),
                )
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            CompositionLocalProvider(
                LocalContentColor provides ElementTheme.colors.textSecondary,
                LocalTextStyle provides ElementTheme.typography.fontBodySmRegular,
            ) {
                Text(text = preview.siteName ?: preview.hostName, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            preview.title?.let {
                CompositionLocalProvider(
                    LocalContentColor provides ElementTheme.colors.textPrimary,
                    LocalTextStyle provides ElementTheme.typography.fontBodyMdMedium,
                ) {
                    Text(text = it, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            preview.description?.let {
                CompositionLocalProvider(
                    LocalContentColor provides ElementTheme.colors.textSecondary,
                    LocalTextStyle provides ElementTheme.typography.fontBodySmRegular,
                ) {
                    Text(text = it, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

private fun previewImageModel(imageUrl: String): Any? {
    return when {
        imageUrl.startsWith("mxc://") -> MediaRequestData(
            source = MediaSource(imageUrl),
            kind = MediaRequestData.Kind.Thumbnail(width = 640, height = 360),
        )
        imageUrl.startsWith("http://") || imageUrl.startsWith("https://") -> imageUrl
        else -> null
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemUrlPreviewViewPreview() = ElementPreview {
    TimelineItemUrlPreviewView(
        preview = UrlPreviewData(
            url = "https://example.org/article",
            title = "Example Article Title That May Be Long",
            description = "A brief description of the article content that gives context to the reader.",
            imageUrl = "https://example.org/image.jpg",
            siteName = "Example",
            hostName = "example.org",
        ),
        onClick = {},
        onLongClick = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemUrlPreviewViewNoImagePreview() = ElementPreview {
    TimelineItemUrlPreviewView(
        preview = UrlPreviewData(
            url = "https://example.org/article",
            title = "Example Article Title",
            description = "A brief description of the article content.",
            imageUrl = null,
            siteName = null,
            hostName = "example.org",
        ),
        onClick = {},
        onLongClick = {},
    )
}
