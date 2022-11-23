package io.element.android.x.features.roomlist.components

import Avatar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.placeholder.material.placeholder
import io.element.android.x.features.roomlist.model.RoomListRoomSummary

private val minHeight = 72.dp

@Composable
internal fun RoomSummaryRow(
    modifier: Modifier = Modifier,
    room: RoomListRoomSummary,
    onClick: (RoomListRoomSummary) -> Unit
) {

    val clickModifier = if (room.isPlaceholder) {
        Modifier
    } else {
        Modifier.clickable(
            onClick = { onClick(room) },
            indication = rememberRipple(),
            interactionSource = remember { MutableInteractionSource() }
        )
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .then(clickModifier)
    ) {
        DefaultRoomSummaryRow(room = room)
    }

}

@Composable
internal fun DefaultRoomSummaryRow(
    room: RoomListRoomSummary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = CenterVertically
    ) {
        Avatar(
            room.avatarData,
            modifier = Modifier.placeholder(room.isPlaceholder, shape = CircleShape)
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)
                .alignByBaseline()
                .weight(1f)
        ) {
            // Name
            Text(
                modifier = Modifier
                    .placeholder(room.isPlaceholder, shape = TextPlaceholderShape),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                text = room.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Last Message
            Text(
                modifier = Modifier.placeholder(room.isPlaceholder, shape = TextPlaceholderShape),
                text = room.lastMessage?.toString().orEmpty(),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // Timestamp and Unread
        Column(
            modifier = Modifier
                .alignByBaseline(),
        ) {
            Text(
                modifier = Modifier.placeholder(room.isPlaceholder, shape = TextPlaceholderShape),
                fontSize = 12.sp,
                text = room.timestamp ?: "",
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.size(4.dp))
            val unreadIndicatorColor =
                if (room.hasUnread) MaterialTheme.colorScheme.primary else Color.Transparent
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(unreadIndicatorColor)
                    .align(Alignment.End),
            )
        }
    }
}

val TextPlaceholderShape = PercentRectangleSizeShape(0.5f)

class PercentRectangleSizeShape(private val percent: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val halfPercent = percent / 2f
        val path = Path().apply {
            val rect = Rect(
                0f,
                size.height * halfPercent,
                size.width,
                size.height - (size.height * halfPercent)
            )
            addRect(rect)
            close()
        }
        return Outline.Generic(path)
    }
}
