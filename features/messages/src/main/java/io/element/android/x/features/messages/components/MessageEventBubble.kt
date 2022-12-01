package io.element.android.x.features.messages.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.element.android.x.designsystem.*
import io.element.android.x.features.messages.model.MessagesItemGroupPosition

private val BUBBLE_RADIUS = 16.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageEventBubble(
    groupPosition: MessagesItemGroupPosition,
    isMine: Boolean,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    fun bubbleShape(): Shape {
        return when (groupPosition) {
            MessagesItemGroupPosition.First -> if (isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(BUBBLE_RADIUS, BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp)
            }
            MessagesItemGroupPosition.Middle -> if (isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, 0.dp, 0.dp, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp)
            }
            MessagesItemGroupPosition.Last -> if (isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, 0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS, BUBBLE_RADIUS)
            }
            MessagesItemGroupPosition.None ->
                RoundedCornerShape(
                    BUBBLE_RADIUS,
                    BUBBLE_RADIUS,
                    BUBBLE_RADIUS,
                    BUBBLE_RADIUS
                )
        }
    }

    fun Modifier.offsetForItem(): Modifier {
        return if (isMine) {
            offset(y = -(12.dp))
        } else {
            offset(x = 20.dp, y = -(12.dp))
        }
    }

    val backgroundBubbleColor = if (isMine) {
        if (LocalIsDarkTheme.current) {
            SystemGrey5Dark
        } else {
            SystemGrey5Light
        }
    } else {
        if (LocalIsDarkTheme.current) {
            SystemGrey6Dark
        } else {
            SystemGrey6Light
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