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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.placeholder.material.placeholder
import io.element.android.x.features.roomlist.model.RoomListRoomSummary
import io.element.android.x.matrix.core.RoomId

@Composable
internal fun RoomItem(
    modifier: Modifier = Modifier,
    room: RoomListRoomSummary,
    onClick: (RoomId) -> Unit
) {
    if (room.isPlaceholder) {
        return PlaceholderRoomItem(modifier = modifier, room = room)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onClick(room.roomId) },
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = CenterVertically
        ) {
            Avatar(room.avatarData)
            Column(
                modifier = Modifier
                    .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)
                    .alignByBaseline()
                    .weight(1f)
            ) {
                // Name
                Text(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    text = room.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Last Message
                Text(
                    text = room.lastMessage?.toString().orEmpty(),
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 20.sp,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Timestamp and Unread
            Column(
                modifier = Modifier
                    .alignByBaseline(),
            ) {
                Text(
                    fontSize = 12.sp,
                    text = room.timestamp ?: "",
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier.size(4.dp))
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
}

@Composable
internal fun PlaceholderRoomItem(
    modifier: Modifier = Modifier,
    room: RoomListRoomSummary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .size(room.avatarData.size.dp)
                .clip(CircleShape)
                .placeholder(true),
            text = ""
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)
                .weight(1f)
        ) {
            Text(
                modifier = Modifier
                    .size(width = 80.dp, height = 12.dp)
                    .placeholder(visible = true),
                text = "",
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                modifier = Modifier
                    .size(width = 160.dp, height = 12.dp)
                    .placeholder(visible = true),
                text = "",
            )
        }
        Column {
            Text(
                modifier = Modifier
                    .size(width = 24.dp, height = 12.dp)
                    .placeholder(visible = true),
                text = "",
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.size(4.dp))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .align(Alignment.End),
            )
        }
    }
}