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

package io.element.android.features.roomlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.placeholder.material.placeholder
import io.element.android.features.roomlist.model.RoomListRoomSummary
import io.element.android.features.roomlist.model.RoomListRoomSummaryProvider
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.roomListPlaceHolder
import io.element.android.libraries.designsystem.theme.roomListRoomMessage
import io.element.android.libraries.designsystem.theme.roomListRoomMessageDate
import io.element.android.libraries.designsystem.theme.roomListRoomName
import io.element.android.libraries.designsystem.theme.roomListUnreadIndicator

private val minHeight = 72.dp

@Composable
internal fun RoomSummaryRow(
    room: RoomListRoomSummary,
    modifier: Modifier = Modifier,
    onClick: (RoomListRoomSummary) -> Unit = {},
) {
    val clickModifier = if (room.isPlaceholder) {
        modifier
    } else {
        modifier.clickable(
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
            modifier = Modifier.placeholder(
                visible = room.isPlaceholder,
                shape = CircleShape,
                color = ElementTheme.colors.roomListPlaceHolder(),
            )
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)
                .alignByBaseline()
                .weight(1f)
        ) {
            // Name
            Text(
                modifier = Modifier.placeholder(
                    visible = room.isPlaceholder,
                    shape = TextPlaceholderShape,
                    color = ElementTheme.colors.roomListPlaceHolder(),
                ),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                text = room.name,
                color = MaterialTheme.roomListRoomName(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Last Message
            Text(
                modifier = Modifier.placeholder(
                    visible = room.isPlaceholder,
                    shape = TextPlaceholderShape,
                    color = ElementTheme.colors.roomListPlaceHolder(),
                ),
                text = room.lastMessage?.toString().orEmpty(),
                color = MaterialTheme.roomListRoomMessage(),
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
                modifier = Modifier.placeholder(
                    visible = room.isPlaceholder,
                    shape = TextPlaceholderShape,
                    color = ElementTheme.colors.roomListPlaceHolder(),
                ),
                fontSize = 12.sp,
                text = room.timestamp ?: "",
                color = MaterialTheme.roomListRoomMessageDate(),
            )
            Spacer(Modifier.size(4.dp))
            val unreadIndicatorColor =
                if (room.hasUnread) MaterialTheme.roomListUnreadIndicator() else Color.Transparent
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

@Preview
@Composable
internal fun RoomSummaryRowLightPreview(@PreviewParameter(RoomListRoomSummaryProvider::class) data: RoomListRoomSummary) =
    ElementPreviewLight { ContentToPreview(data) }

@Preview
@Composable
internal fun RoomSummaryRowDarkPreview(@PreviewParameter(RoomListRoomSummaryProvider::class) data: RoomListRoomSummary) =
    ElementPreviewDark { ContentToPreview(data) }

@Composable
private fun ContentToPreview(data: RoomListRoomSummary) {
    RoomSummaryRow(data)
}
