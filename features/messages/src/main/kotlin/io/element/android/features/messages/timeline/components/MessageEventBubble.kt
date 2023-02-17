/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.features.messages.timeline.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.timeline.model.TimelineItemGroupPosition
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Surface

private val BUBBLE_RADIUS = 16.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageEventBubble(
    state: BubbleState,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    fun bubbleShape(): Shape {
        return when (state.groupPosition) {
            TimelineItemGroupPosition.First -> if (state.isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(BUBBLE_RADIUS, BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp)
            }
            TimelineItemGroupPosition.Middle -> if (state.isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, 0.dp, 0.dp, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp)
            }
            TimelineItemGroupPosition.Last -> if (state.isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, 0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS, BUBBLE_RADIUS)
            }
            TimelineItemGroupPosition.None ->
                RoundedCornerShape(
                    BUBBLE_RADIUS,
                    BUBBLE_RADIUS,
                    BUBBLE_RADIUS,
                    BUBBLE_RADIUS
                )
        }
    }

    fun Modifier.offsetForItem(): Modifier {
        return if (state.isMine) {
            offset(y = -(12.dp))
        } else {
            offset(x = 20.dp, y = -(12.dp))
        }
    }

    val backgroundBubbleColor = if (state.isHighlighted) {
        ElementTheme.colors.messageHighlightedBackground
    } else {
        if (state.isMine) {
            ElementTheme.colors.messageFromMeBackground
        } else {
            ElementTheme.colors.messageFromOtherBackground
        }
    }
    val bubbleShape = bubbleShape()
    Surface(
        modifier = modifier
            .widthIn(min = 80.dp)
            .offsetForItem()
            .clip(bubbleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                indication = rememberRipple(),
                interactionSource = interactionSource
            ),
        color = backgroundBubbleColor,
        shape = bubbleShape,
        content = content
    )
}

@Preview
@Composable
internal fun MessageEventBubbleLightPreview(@PreviewParameter(BubbleStateProvider::class) state: BubbleState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun MessageEventBubbleDarkPreview(@PreviewParameter(BubbleStateProvider::class) state: BubbleState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: BubbleState) {
    // Due to y offset, surround with a Box
    Box(
        modifier = Modifier
            .size(width = 240.dp, height = 64.dp)
            .padding(8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        MessageEventBubble(
            state = state,
            interactionSource = MutableInteractionSource(),
        ) {
            Spacer(modifier = Modifier.size(width = 120.dp, height = 32.dp))
        }
    }
}
