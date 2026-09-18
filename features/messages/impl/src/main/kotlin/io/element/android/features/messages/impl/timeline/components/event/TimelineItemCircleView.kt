/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import android.view.LayoutInflater
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.circlemessages.timeline.CircleMessageEvent
import io.element.android.features.messages.impl.circlemessages.timeline.CircleMessageState
import io.element.android.features.messages.impl.timeline.components.MessageEventBubbleDefaults
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemCircleContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemCircleContentPreviewParam
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemCircleContent
import io.element.android.libraries.designsystem.components.blurhash.blurHashBackground
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.ui.media.MAX_THUMBNAIL_HEIGHT
import io.element.android.libraries.matrix.ui.media.MAX_THUMBNAIL_WIDTH
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.ui.strings.CommonStrings

private val CIRCLE_PROGRESS_STROKE = 2.dp
private val CIRCLE_PROGRESS_GAP = 3.dp
private val CIRCLE_RING = CIRCLE_PROGRESS_GAP + CIRCLE_PROGRESS_STROKE
private const val CIRCLE_IDLE_RATIO = 0.75f
private const val CIRCLE_PLAY_FALLBACK_DP = 240f

internal fun circlePlayMediaSize(maxOuterWidthDp: Float): Float {
    if (!maxOuterWidthDp.isFinite()) return CIRCLE_PLAY_FALLBACK_DP
    return (maxOuterWidthDp - CIRCLE_RING.value * 2).coerceAtLeast(0f)
}

internal fun circleIdleMediaSize(playMediaSizeDp: Float): Float = playMediaSizeDp * CIRCLE_IDLE_RATIO

@Composable
fun TimelineItemCircleView(
    state: CircleMessageState,
    content: TimelineItemCircleContent,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val playSize = circlePlayMediaSize(if (constraints.hasBoundedWidth) maxWidth.value else Float.POSITIVE_INFINITY).dp
        val idleSize = circleIdleMediaSize(playSize.value).dp
        TimelineItemCircleViewContent(
            state = state,
            content = content,
            mediaSize = if (state.isExpanded) playSize else idleSize,
        )
    }
}

@Composable
private fun TimelineItemCircleViewContent(
    state: CircleMessageState,
    content: TimelineItemCircleContent,
    mediaSize: Dp,
) {
    val animatedMediaSize by animateDpAsState(
        targetValue = mediaSize,
        label = "circleSize",
    )
    val outerSize = animatedMediaSize + CIRCLE_RING * 2
    val a11y = stringResource(CommonStrings.screen_room_circle_message)
    Box(
        modifier = Modifier
            .size(outerSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = a11y,
            ) {
                state.eventSink(CircleMessageEvent.PlayPause)
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(animatedMediaSize)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val lastFrame = state.lastFrame
            if (lastFrame != null) {
                Image(
                    bitmap = lastFrame.asImageBitmap(),
                    contentDescription = a11y,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(animatedMediaSize),
                )
            } else {
                val thumbnailData = MediaRequestData(
                    source = content.thumbnailSource ?: content.mediaSource,
                    kind = MediaRequestData.Kind.Thumbnail(MAX_THUMBNAIL_WIDTH, MAX_THUMBNAIL_HEIGHT),
                )
                AsyncImage(
                    modifier = Modifier
                        .size(animatedMediaSize)
                        .blurHashBackground(content.blurHash)
                        .background(ElementTheme.colors.bgSubtlePrimary),
                    model = thumbnailData,
                    contentDescription = a11y,
                    contentScale = ContentScale.Crop,
                )
            }
            val showPlayer = state.isPlaying &&
                state.exoPlayer != null &&
                animatedMediaSize == mediaSize &&
                !LocalInspectionMode.current
            if (showPlayer) {
                AndroidView(
                    modifier = Modifier
                        .size(animatedMediaSize)
                        .background(Color.Transparent),
                    factory = { context ->
                        (LayoutInflater.from(context).inflate(R.layout.view_circle_player, null) as PlayerView).apply {
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                        }
                    },
                    update = { playerView ->
                        state.eventSink(CircleMessageEvent.BindPlayerView(playerView))
                    },
                    onRelease = { playerView ->
                        state.eventSink(CircleMessageEvent.UnbindPlayerView(playerView))
                    },
                )
            }
        }
        val progressColor = ElementTheme.colors.iconAccentTertiary
        Canvas(modifier = Modifier.size(outerSize)) {
            val strokeWidth = CIRCLE_PROGRESS_STROKE.toPx()
            val inset = strokeWidth / 2f
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = state.progress * 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(this.size.width - strokeWidth, this.size.height - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        if (state.button == CircleMessageState.Button.Downloading) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = Color.White,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemCircleViewPreview(
    @PreviewParameter(TimelineItemCircleContentPreviewParam::class) content: TimelineItemCircleContent,
) = ElementPreview {
    TimelineItemCirclePreviewHost {
        TimelineItemCircleView(
            state = CircleMessageState(
                button = CircleMessageState.Button.Play,
                progress = 0.35f,
                isPlaying = false,
                durationMs = 8_000,
                isExpanded = false,
                lastFrame = null,
                exoPlayer = null,
                eventSink = {},
            ),
            content = content,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemCircleViewPlayingPreview() = ElementPreview {
    TimelineItemCirclePreviewHost {
        TimelineItemCircleView(
            state = CircleMessageState(
                button = CircleMessageState.Button.Pause,
                progress = 0.6f,
                isPlaying = true,
                durationMs = 8_000,
                isExpanded = true,
                lastFrame = null,
                exoPlayer = null,
                eventSink = {},
            ),
            content = aTimelineItemCircleContent(),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemCircleViewDownloadingPreview() = ElementPreview {
    TimelineItemCirclePreviewHost {
        TimelineItemCircleView(
            state = CircleMessageState(
                button = CircleMessageState.Button.Downloading,
                progress = 0f,
                isPlaying = false,
                durationMs = 8_000,
                isExpanded = false,
                lastFrame = null,
                exoPlayer = null,
                eventSink = {},
            ),
            content = aTimelineItemCircleContent(),
        )
    }
}

private val CIRCLE_PREVIEW_COLUMN_WIDTH = 360.dp * MessageEventBubbleDefaults.BUBBLE_WIDTH_RATIO

@Composable
private fun TimelineItemCirclePreviewHost(content: @Composable () -> Unit) {
    Box(modifier = Modifier.width(CIRCLE_PREVIEW_COLUMN_WIDTH)) {
        content()
    }
}
