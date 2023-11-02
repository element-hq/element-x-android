/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.roomlist.impl.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomListRoomSummaryProvider
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.designsystem.atomic.atoms.UnreadIndicatorAtom
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.roomListRoomMessage
import io.element.android.libraries.designsystem.theme.roomListRoomMessageDate
import io.element.android.libraries.designsystem.theme.roomListRoomName
import io.element.android.libraries.designsystem.theme.unreadIndicator
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

internal val minHeight = 84.dp

@Composable
internal fun RoomSummaryRow(
    room: RoomListRoomSummary,
    onClick: (RoomListRoomSummary) -> Unit,
    onLongClick: (RoomListRoomSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (room.isPlaceholder) {
        RoomSummaryPlaceholderRow(
            modifier = modifier,
        )
    } else {
        RoomSummaryRealRow(
            room = room,
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoomSummaryRealRow(
    room: RoomListRoomSummary,
    onClick: (RoomListRoomSummary) -> Unit,
    onLongClick: (RoomListRoomSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clickModifier = Modifier.combinedClickable(
        onClick = { onClick(room) },
        onLongClick = { onLongClick(room) },
        indication = rememberRipple(),
        interactionSource = remember { MutableInteractionSource() }
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .then(clickModifier)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp)
            .height(IntrinsicSize.Min),
    ) {
        Avatar(
            room
                .avatarData,
            modifier = Modifier
                .align(Alignment.CenterVertically)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                NameAndTimestampRow(room = room)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                LastMessageAndIndicatorRow(room = room)
            }
        }
    }
}

@Composable
private fun RowScope.NameAndTimestampRow(room: RoomListRoomSummary) {
    // Name
    Text(
        modifier = Modifier
            .weight(1f)
            .padding(end = 16.dp),
        style = ElementTheme.typography.fontBodyLgMedium,
        text = room.name,
        color = MaterialTheme.roomListRoomName(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    // Timestamp
    Text(
        text = room.timestamp ?: "",
        style = ElementTheme.typography.fontBodySmRegular,
        color = if (room.hasUnread) {
            ElementTheme.colors.unreadIndicator
        } else {
            MaterialTheme.roomListRoomMessageDate()
        },
    )
}

@Composable
private fun RowScope.LastMessageAndIndicatorRow(room: RoomListRoomSummary) {
    // Last Message
    val attributedLastMessage = room.lastMessage as? AnnotatedString
        ?: AnnotatedString(room.lastMessage.orEmpty().toString())
    Text(
        modifier = Modifier
            .weight(1f)
            .padding(end = 28.dp),
        text = attributedLastMessage,
        color = MaterialTheme.roomListRoomMessage(),
        style = ElementTheme.typography.fontBodyMdRegular,
        minLines = 2,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )

    // Unread
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NotificationIcon(room)
        if (room.hasUnread) {
            UnreadIndicatorAtom(
                modifier = Modifier.padding(vertical = 3.dp),
            )
        }
    }
}

@Composable
private fun NotificationIcon(room: RoomListRoomSummary) {
    val tint = if (room.hasUnread) ElementTheme.colors.unreadIndicator else ElementTheme.colors.iconQuaternary
    when (room.notificationMode) {
        null, RoomNotificationMode.ALL_MESSAGES -> return
        RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY ->
            Icon(
                modifier = Modifier.size(16.dp),
                contentDescription = stringResource(CommonStrings.screen_notification_settings_mode_mentions),
                imageVector = ImageVector.vectorResource(CommonDrawables.ic_compound_mention),
                tint = tint,
            )
        RoomNotificationMode.MUTE ->
            Icon(
                modifier = Modifier.size(16.dp),
                contentDescription = stringResource(CommonStrings.common_mute),
                imageVector = ImageVector.vectorResource(CommonDrawables.ic_compound_notifications_solid_off),
                tint = tint,
            )
    }
}

@PreviewsDayNight
@Composable
internal fun RoomSummaryRowPreview(@PreviewParameter(RoomListRoomSummaryProvider::class) data: RoomListRoomSummary) = ElementPreview {
    RoomSummaryRow(
        room = data,
        onClick = {},
        onLongClick = {}
    )
}
