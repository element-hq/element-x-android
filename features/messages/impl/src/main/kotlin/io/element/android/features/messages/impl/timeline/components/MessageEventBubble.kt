/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.bubble.BubbleState
import io.element.android.features.messages.impl.timeline.model.bubble.BubbleStateProvider
import io.element.android.libraries.core.extensions.to01
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.text.toPx
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.messageFromMeBackground
import io.element.android.libraries.designsystem.theme.messageFromOtherBackground
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.utils.time.isTalkbackActive

private val BUBBLE_RADIUS = 12.dp
private val avatarRadius = AvatarSize.TimelineSender.dp / 2

private val MIN_BUBBLE_WIDTH = 80.dp

@Composable
fun MessageEventBubble(
    state: BubbleState,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val clickableModifier = if (isTalkbackActive()) {
        Modifier
    } else {
        Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                indication = ripple(),
                interactionSource = interactionSource
            )
            .onKeyboardContextMenuAction(onLongClick)
    }

    // Ignore state.isHighlighted for now, we need a design decision on it.
    val backgroundBubbleColor = MessageEventBubbleDefaults.backgroundBubbleColor(state.isMine)
    val bubbleShape = remember(state) { MessageEventBubbleDefaults.shape(state.cutTopStart, state.groupPosition, state.isMine) }
    val radiusPx = (avatarRadius + SENDER_AVATAR_BORDER_WIDTH).toPx()
    val yOffsetPx = -(NEGATIVE_MARGIN_FOR_BUBBLE + avatarRadius).toPx()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer {
                shape = bubbleShape
                clip = true
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                drawRect(backgroundBubbleColor)
                drawContent()
                if (state.cutTopStart) {
                    drawCircle(
                        color = Color.Black,
                        center = Offset(
                            x = if (isRtl) size.width else 0f,
                            y = yOffsetPx,
                        ),
                        radius = radiusPx,
                        blendMode = BlendMode.Clear,
                    )
                }
            },
        // Need to set the contentAlignment again (it's already set in TimelineItemEventRow), for the case
        // when content width is low.
        contentAlignment = if (state.isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .testTag(TestTags.messageBubble)
                .widthIn(
                    min = MIN_BUBBLE_WIDTH,
                    max = (constraints.maxWidth * MessageEventBubbleDefaults.BUBBLE_WIDTH_RATIO)
                        .toInt()
                        .toDp()
                )
                .then(clickableModifier),
            content = content,
        )
    }
}

object MessageEventBubbleDefaults {
    fun shape(cutTopStart: Boolean, groupPosition: TimelineItemGroupPosition, isMine: Boolean): Shape {
        val topLeftCorner = if (cutTopStart) 0.dp else BUBBLE_RADIUS
        return when (groupPosition) {
            TimelineItemGroupPosition.First -> if (isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(topLeftCorner, BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp)
            }
            TimelineItemGroupPosition.Middle -> if (isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, 0.dp, 0.dp, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp)
            }
            TimelineItemGroupPosition.Last -> if (isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, 0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS, BUBBLE_RADIUS)
            }
            TimelineItemGroupPosition.None ->
                RoundedCornerShape(
                    topLeftCorner,
                    BUBBLE_RADIUS,
                    BUBBLE_RADIUS,
                    BUBBLE_RADIUS
                )
        }
    }

    @Composable
    fun backgroundBubbleColor(isMine: Boolean): Color {
        return if (isMine) {
            ElementTheme.colors.messageFromMeBackground
        } else {
            ElementTheme.colors.messageFromOtherBackground
        }
    }

    // Design says: The maximum width of a bubble is still 3/4 of the screen width. But try with 78% now.
    const val BUBBLE_WIDTH_RATIO = 0.78f
}

@PreviewsDayNight
@Composable
internal fun MessageEventBubblePreview(@PreviewParameter(BubbleStateProvider::class) state: BubbleState) = ElementPreview {
    // Due to position offset, surround with a Box
    Box(
        modifier = Modifier
            .size(width = 240.dp, height = 64.dp)
            .padding(vertical = 8.dp),
        contentAlignment = if (state.isMine) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        MessageEventBubble(
            state = state,
            interactionSource = remember { MutableInteractionSource() },
            onClick = {},
            onLongClick = {},
        ) {
            // Render the state as a text to better understand the previews
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 32.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${state.groupPosition.javaClass.simpleName} isMine:${state.isMine.to01()}",
                    style = ElementTheme.typography.fontBodyXsRegular,
                )
            }
        }
    }
}
