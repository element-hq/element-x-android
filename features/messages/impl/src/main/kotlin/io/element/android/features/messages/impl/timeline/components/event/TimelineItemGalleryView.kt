/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import android.text.SpannedString
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.model.event.GalleryItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemGalleryContent
import io.element.android.libraries.designsystem.components.blurhash.blurHashBackground
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.bgSubtleTertiary
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.utils.time.formatShort
import io.element.android.wysiwyg.compose.EditorStyledText
import io.element.android.wysiwyg.link.Link
import kotlin.time.Duration.Companion.seconds

private const val MAX_TILES = 5
private val GRID_SPACING = 4.dp
private val GROUP_CORNER_RADIUS = 6.dp

@Composable
fun TimelineItemGalleryView(
    content: TimelineItemGalleryContent,
    onContentClick: ((Int) -> Unit)?,
    onLongClick: (() -> Unit)?,
    onLinkClick: (Link) -> Unit,
    onLinkLongClick: (Link) -> Unit,
    onContentLayoutChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (content.items.isEmpty()) return

    val totalItems = content.items.size
    val showOverflow = totalItems > MAX_TILES - 1
    val overflowCount = totalItems - (MAX_TILES - 1)

    Column(modifier = modifier) {
        val containerModifier = Modifier.clip(RoundedCornerShape(GROUP_CORNER_RADIUS))

        BoxWithConstraints(
            modifier = containerModifier.fillMaxWidth(),
        ) {
            val availableWidth = maxWidth
            val squareSize = (availableWidth - GRID_SPACING) / 2

            Column(
                verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
            ) {
                when {
                    totalItems == 1 -> {
                        SingleItemLayout(
                            item = content.items[0],
                            availableWidth = availableWidth,
                            tileHeight = squareSize,
                            onClick = onContentClick?.let { { it(0) } },
                            onLongClick = onLongClick,
                        )
                    }
                    totalItems == 2 -> {
                        TwoItemLayout(
                            items = content.items,
                            availableWidth = availableWidth,
                            tileHeight = squareSize,
                            onContentClick = onContentClick,
                            onLongClick = onLongClick,
                        )
                    }
                    totalItems == 3 -> {
                        ThreeItemLayout(
                            items = content.items,
                            availableWidth = availableWidth,
                            topRowHeight = squareSize,
                            bottomRowHeight = squareSize,
                            onContentClick = onContentClick,
                            onLongClick = onLongClick,
                        )
                    }
                    totalItems >= 4 -> {
                        val bottomRowTileCount = minOf(if (showOverflow) 3 else (totalItems - 2), 3)
                        val topRowTileCount = 2
                        val topRowHeight = (availableWidth - GRID_SPACING * (topRowTileCount - 1)) / topRowTileCount
                        val bottomRowHeight = (availableWidth - GRID_SPACING * (bottomRowTileCount - 1)) / bottomRowTileCount
                        FourPlusItemLayout(
                            items = content.items,
                            showOverflow = showOverflow,
                            overflowCount = overflowCount,
                            availableWidth = availableWidth,
                            topRowHeight = topRowHeight,
                            bottomRowHeight = bottomRowHeight,
                            onContentClick = onContentClick,
                            onLongClick = onLongClick,
                        )
                    }
                }
            }
        }

        if (content.showCaption) {
            Spacer(modifier = Modifier.height(8.dp))
            val caption = if (LocalInspectionMode.current) {
                SpannedString(content.caption)
            } else {
                content.formattedCaption ?: SpannedString(content.caption)
            }
            CompositionLocalProvider(
                LocalContentColor provides ElementTheme.colors.textPrimary,
                LocalTextStyle provides ElementTheme.typography.fontBodyLgRegular
            ) {
                EditorStyledText(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .widthIn(min = 120.dp),
                    text = caption,
                    style = ElementRichTextEditorStyle.textStyle(),
                    onLinkClickedListener = onLinkClick,
                    onLinkLongClickedListener = onLinkLongClick,
                    releaseOnDetach = false,
                )
            }
        }
    }
}

// ── Layout: 1 item ──────────────────────────────────────────────────────────────

@Composable
private fun SingleItemLayout(
    item: GalleryItem,
    availableWidth: androidx.compose.ui.unit.Dp,
    tileHeight: androidx.compose.ui.unit.Dp,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
) {
    GalleryItemCell(
        item = item,
        isLast = false,
        remainingCount = 0,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier
            .width(availableWidth)
            .height(tileHeight),
    )
}

// ── Layout: 2 items ─────────────────────────────────────────────────────────

@Composable
private fun TwoItemLayout(
    items: List<GalleryItem>,
    availableWidth: androidx.compose.ui.unit.Dp,
    tileHeight: androidx.compose.ui.unit.Dp,
    onContentClick: ((Int) -> Unit)?,
    onLongClick: (() -> Unit)?,
) {
    Column(
        modifier = Modifier.width(availableWidth),
        verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
    ) {
        items.forEachIndexed { index, item ->
            GalleryItemCell(
                item = item,
                isLast = false,
                remainingCount = 0,
                onClick = onContentClick?.let { { it(index) } },
                onLongClick = onLongClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tileHeight),
            )
        }
    }
}

// ── Layout: 3 items: 1 rectangle on top + 2 squares below ─────────────────────

@Composable
private fun ThreeItemLayout(
    items: List<GalleryItem>,
    availableWidth: androidx.compose.ui.unit.Dp,
    topRowHeight: androidx.compose.ui.unit.Dp,
    bottomRowHeight: androidx.compose.ui.unit.Dp,
    onContentClick: ((Int) -> Unit)?,
    onLongClick: (() -> Unit)?,
) {
    Column(
        modifier = Modifier.width(availableWidth),
        verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
    ) {
        GalleryItemCell(
            item = items[0],
            isLast = false,
            remainingCount = 0,
            onClick = onContentClick?.let { { it(0) } },
            onLongClick = onLongClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(topRowHeight),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
        ) {
            GalleryItemCell(
                item = items[1],
                isLast = false,
                remainingCount = 0,
                onClick = onContentClick?.let { { it(1) } },
                onLongClick = onLongClick,
                modifier = Modifier
                    .weight(1f)
                    .height(bottomRowHeight),
            )
            GalleryItemCell(
                item = items[2],
                isLast = false,
                remainingCount = 0,
                onClick = onContentClick?.let { { it(2) } },
                onLongClick = onLongClick,
                modifier = Modifier
                    .weight(1f)
                    .height(bottomRowHeight),
            )
        }
    }
}

// ── Layout: 4+ items ─────────────────────────────────────────────────────────

@Composable
private fun FourPlusItemLayout(
    items: List<GalleryItem>,
    showOverflow: Boolean,
    overflowCount: Int,
    availableWidth: androidx.compose.ui.unit.Dp,
    topRowHeight: androidx.compose.ui.unit.Dp,
    bottomRowHeight: androidx.compose.ui.unit.Dp,
    onContentClick: ((Int) -> Unit)?,
    onLongClick: (() -> Unit)?,
) {
    Column(
        modifier = Modifier.width(availableWidth),
        verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
        ) {
            GalleryItemCell(
                item = items[0],
                isLast = false,
                remainingCount = 0,
                onClick = onContentClick?.let { { it(0) } },
                onLongClick = onLongClick,
                modifier = Modifier
                    .weight(1f)
                    .height(topRowHeight),
            )
            GalleryItemCell(
                item = items[1],
                isLast = false,
                remainingCount = 0,
                onClick = onContentClick?.let { { it(1) } },
                onLongClick = onLongClick,
                modifier = Modifier
                    .weight(1f)
                    .height(topRowHeight),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
        ) {
            val bottomRowItems = if (showOverflow) 3 else minOf(items.size - 2, 3)
            for (i in 0 until bottomRowItems) {
                val itemIndex = 2 + i
                if (itemIndex < items.size) {
                    val isOverflowItem = showOverflow && i == bottomRowItems - 1
                    GalleryItemCell(
                        item = items[itemIndex],
                        isLast = isOverflowItem,
                        remainingCount = if (isOverflowItem) overflowCount else 0,
                        onClick = onContentClick?.let { { it(itemIndex) } },
                        onLongClick = onLongClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(bottomRowHeight),
                    )
                }
            }
        }
    }
}

// ── Gallery item cell ────────────────────────────────────────────────────────

@Composable
private fun GalleryItemCell(
    item: GalleryItem,
    isLast: Boolean,
    remainingCount: Int,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .blurHashBackground(item.blurhash, alpha = 0.9f)
            .then(
                if (onClick != null) {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = item.thumbnailMediaRequestData,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            contentDescription = item.filename,
        )

        if (item.isVideo) {
            VideoOverlay(duration = item.duration)
        }

        if (item.isAudio) {
            AudioIconOverlay()
        }

        if (item.isFile) {
            FileIconOverlay()
        }

        if (isLast && remainingCount > 0) {
            RemainingCountOverlay(count = remainingCount)
        }
    }
}

// ── Overlays ─────────────────────────────────────────────────────────────────

@Composable
private fun VideoOverlay(duration: kotlin.time.Duration) {
    val gradientColor = ElementTheme.colors.bgCanvasDefault

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(gradientColor.copy(alpha = 0f), gradientColor.copy(alpha = 1f))
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = CompoundIcons.VideoCallSolid(),
                contentDescription = null,
                tint = ElementTheme.colors.textPrimary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = duration.formatShort(),
                color = ElementTheme.colors.textPrimary,
                style = ElementTheme.typography.fontBodySmMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AudioIconOverlay() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = CompoundIcons.MicOnSolid(),
            contentDescription = stringResource(CommonStrings.common_audio),
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun FileIconOverlay() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = CompoundIcons.Attachment(),
            contentDescription = stringResource(CommonStrings.common_file),
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun RemainingCountOverlay(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ElementTheme.colors.bgSubtleTertiary.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+$count",
            color = Color.White,
            style = ElementTheme.typography.fontHeadingXlBold,
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@PreviewsDayNight
@Composable
internal fun TimelineItemGalleryViewPreview() = ElementPreview {
    TimelineItemGalleryView(
        content = TimelineItemGalleryContent(
            body = "Gallery",
            caption = "My vacation photos",
            formattedCaption = null,
            isEdited = false,
            items = listOf(
                createSampleGalleryItem(isVideo = false),
                createSampleGalleryItem(isVideo = true, duration = 65.seconds),
                createSampleGalleryItem(isVideo = false),
                createSampleGalleryItem(isVideo = false),
            ),
        ),
        onContentClick = {},
        onLongClick = {},
        onLinkClick = {},
        onLinkLongClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemGalleryViewSingleItemPreview() = ElementPreview {
    TimelineItemGalleryView(
        content = TimelineItemGalleryContent(
            body = "Gallery",
            caption = null,
            formattedCaption = null,
            isEdited = false,
            items = listOf(
                createSampleGalleryItem(isVideo = false),
            ),
        ),
        onContentClick = {},
        onLongClick = {},
        onLinkClick = {},
        onLinkLongClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemGalleryViewTwoLandscapePreview() = ElementPreview {
    TimelineItemGalleryView(
        content = TimelineItemGalleryContent(
            body = "Gallery",
            caption = null,
            formattedCaption = null,
            isEdited = false,
            items = listOf(
                createSampleGalleryItem(width = 1920, height = 1080),
                createSampleGalleryItem(width = 1600, height = 900),
            ),
        ),
        onContentClick = {},
        onLongClick = {},
        onLinkClick = {},
        onLinkLongClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemGalleryViewTwoPortraitPreview() = ElementPreview {
    TimelineItemGalleryView(
        content = TimelineItemGalleryContent(
            body = "Gallery",
            caption = null,
            formattedCaption = null,
            isEdited = false,
            items = listOf(
                createSampleGalleryItem(width = 1080, height = 1920),
                createSampleGalleryItem(width = 900, height = 1600),
            ),
        ),
        onContentClick = {},
        onLongClick = {},
        onLinkClick = {},
        onLinkLongClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemGalleryViewTwoMixedPreview() = ElementPreview {
    TimelineItemGalleryView(
        content = TimelineItemGalleryContent(
            body = "Gallery",
            caption = null,
            formattedCaption = null,
            isEdited = false,
            items = listOf(
                createSampleGalleryItem(width = 1920, height = 1080),
                createSampleGalleryItem(width = 1080, height = 1920),
            ),
        ),
        onContentClick = {},
        onLongClick = {},
        onLinkClick = {},
        onLinkLongClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemGalleryViewThreeItemsPreview() = ElementPreview {
    TimelineItemGalleryView(
        content = TimelineItemGalleryContent(
            body = "Gallery",
            caption = null,
            formattedCaption = null,
            isEdited = false,
            items = listOf(
                createSampleGalleryItem(isVideo = true, duration = 45.seconds),
                createSampleGalleryItem(isVideo = false),
                createSampleGalleryItem(isVideo = false),
            ),
        ),
        onContentClick = {},
        onLongClick = {},
        onLinkClick = {},
        onLinkLongClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemGalleryViewThree2L1PPreview() = ElementPreview {
    TimelineItemGalleryView(
        content = TimelineItemGalleryContent(
            body = "Gallery",
            caption = "2 landscape + 1 portrait",
            formattedCaption = null,
            isEdited = false,
            items = listOf(
                createSampleGalleryItem(width = 1920, height = 1080),
                createSampleGalleryItem(width = 1600, height = 900),
                createSampleGalleryItem(width = 1080, height = 1920),
            ),
        ),
        onContentClick = {},
        onLongClick = {},
        onLinkClick = {},
        onLinkLongClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemGalleryViewThree1L2PPreview() = ElementPreview {
    TimelineItemGalleryView(
        content = TimelineItemGalleryContent(
            body = "Gallery",
            caption = "1 landscape + 2 portrait",
            formattedCaption = null,
            isEdited = false,
            items = listOf(
                createSampleGalleryItem(width = 1920, height = 1080),
                createSampleGalleryItem(width = 1080, height = 1920),
                createSampleGalleryItem(width = 900, height = 1600),
            ),
        ),
        onContentClick = {},
        onLongClick = {},
        onLinkClick = {},
        onLinkLongClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemGalleryViewFiveItemsPreview() = ElementPreview {
    TimelineItemGalleryView(
        content = TimelineItemGalleryContent(
            body = "Gallery",
            caption = null,
            formattedCaption = null,
            isEdited = false,
            items = listOf(
                createSampleGalleryItem(isVideo = false),
                createSampleGalleryItem(isVideo = false),
                createSampleGalleryItem(isVideo = true, duration = 120.seconds),
                createSampleGalleryItem(isVideo = false),
                createSampleGalleryItem(isVideo = false),
            ),
        ),
        onContentClick = {},
        onLongClick = {},
        onLinkClick = {},
        onLinkLongClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemGalleryViewManyItemsPreview() = ElementPreview {
    TimelineItemGalleryView(
        content = TimelineItemGalleryContent(
            body = "Gallery",
            caption = "Many photos",
            formattedCaption = null,
            isEdited = false,
            items = (1..12).map { createSampleGalleryItem(isVideo = it == 3) },
        ),
        onContentClick = {},
        onLongClick = {},
        onLinkClick = {},
        onLinkLongClick = {},
        onContentLayoutChange = {},
    )
}

// ── Sample data helpers ──────────────────────────────────────────────────────

private fun createSampleGalleryItem(
    isVideo: Boolean = false,
    width: Int = 400,
    height: Int = 300,
    duration: kotlin.time.Duration = kotlin.time.Duration.ZERO,
): GalleryItem {
    return GalleryItem(
        filename = "photo.jpg",
        mimeType = "image/jpeg",
        mediaSource = io.element.android.libraries.matrix.api.media.MediaSource(url = "", json = ""),
        thumbnailSource = null,
        width = width,
        height = height,
        thumbnailWidth = width,
        thumbnailHeight = height,
        blurhash = null,
        isVideo = isVideo,
        isAudio = false,
        isFile = false,
        duration = duration,
    )
}
