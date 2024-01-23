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

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.message.RoomMessage
import io.element.android.libraries.matrix.api.roomlist.RoomSummaryDetails
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun SelectedRoom(
    roomSummary: RoomSummaryDetails,
    onRoomRemoved: (RoomSummaryDetails) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(56.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Avatar(AvatarData(roomSummary.roomId.value, roomSummary.name, roomSummary.avatarUrl, AvatarSize.SelectedRoom))
            Text(
                text = roomSummary.name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(CircleShape)
                .size(20.dp)
                .align(Alignment.TopEnd)
                .clickable(
                    indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { onRoomRemoved(roomSummary) }
                ),
        ) {
            Icon(
                imageVector = CompoundIcons.Close,
                contentDescription = stringResource(id = CommonStrings.action_remove),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun SelectedRoomPreview() = ElementPreview {
    SelectedRoom(
        roomSummary = aRoomSummaryDetails(),
        onRoomRemoved = {},
    )
}

fun aRoomSummaryDetails(
    roomId: RoomId = RoomId("!room:domain"),
    name: String = "roomName",
    canonicalAlias: String? = null,
    isDirect: Boolean = true,
    avatarUrl: String? = null,
    lastMessage: RoomMessage? = null,
    inviter: RoomMember? = null,
    notificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    isDm: Boolean = false,
    numUnreadMentions: Int = 0,
    numUnreadMessages: Int = 0,
) = RoomSummaryDetails(
    roomId = roomId,
    name = name,
    canonicalAlias = canonicalAlias,
    isDirect = isDirect,
    avatarUrl = avatarUrl,
    lastMessage = lastMessage,
    inviter = inviter,
    userDefinedNotificationMode = notificationMode,
    hasRoomCall = hasRoomCall,
    isDm = isDm,
    numUnreadMentions = numUnreadMentions,
    numUnreadMessages = numUnreadMessages,
)
