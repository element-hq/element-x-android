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

package io.element.android.features.messages.impl.mentions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.R
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.textcomposer.mentions.ResolvedMentionSuggestion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun MentionSuggestionsPickerView(
    roomId: RoomId,
    roomName: String?,
    roomAvatarData: AvatarData?,
    memberSuggestions: ImmutableList<ResolvedMentionSuggestion>,
    onSelectSuggestion: (ResolvedMentionSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            memberSuggestions,
            key = { suggestion ->
                when (suggestion) {
                    is ResolvedMentionSuggestion.AtRoom -> "@room"
                    is ResolvedMentionSuggestion.Member -> suggestion.roomMember.userId.value
                }
            }
        ) {
            Column(modifier = Modifier.fillParentMaxWidth()) {
                RoomMemberSuggestionItemView(
                    memberSuggestion = it,
                    roomId = roomId.value,
                    roomName = roomName,
                    roomAvatar = roomAvatarData,
                    onSelectSuggestion = onSelectSuggestion,
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun RoomMemberSuggestionItemView(
    memberSuggestion: ResolvedMentionSuggestion,
    roomId: String,
    roomName: String?,
    roomAvatar: AvatarData?,
    onSelectSuggestion: (ResolvedMentionSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.clickable { onSelectSuggestion(memberSuggestion) }, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        val avatarSize = AvatarSize.TimelineRoom
        val avatarData = when (memberSuggestion) {
            is ResolvedMentionSuggestion.AtRoom -> roomAvatar?.copy(size = avatarSize) ?: AvatarData(roomId, roomName, null, avatarSize)
            is ResolvedMentionSuggestion.Member -> AvatarData(
                memberSuggestion.roomMember.userId.value,
                memberSuggestion.roomMember.displayName,
                memberSuggestion.roomMember.avatarUrl,
                avatarSize,
            )
        }
        val title = when (memberSuggestion) {
            is ResolvedMentionSuggestion.AtRoom -> stringResource(R.string.screen_room_mentions_at_room_title)
            is ResolvedMentionSuggestion.Member -> memberSuggestion.roomMember.displayName
        }

        val subtitle = when (memberSuggestion) {
            is ResolvedMentionSuggestion.AtRoom -> "@room"
            is ResolvedMentionSuggestion.Member -> memberSuggestion.roomMember.userId.value
        }

        Avatar(avatarData = avatarData, modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, top = 8.dp, bottom = 8.dp)
                .align(Alignment.CenterVertically),
        ) {
            title?.let {
                Text(
                    text = it,
                    style = ElementTheme.typography.fontBodyLgRegular,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = subtitle,
                style = ElementTheme.typography.fontBodySmRegular,
                color = ElementTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun MentionSuggestionsPickerViewPreview() {
    ElementPreview {
        val roomMember = RoomMember(
            userId = UserId("@alice:server.org"),
            displayName = null,
            avatarUrl = null,
            membership = RoomMembershipState.JOIN,
            isNameAmbiguous = false,
            powerLevel = 0L,
            normalizedPowerLevel = 0L,
            isIgnored = false,
            role = RoomMember.Role.USER,
        )
        MentionSuggestionsPickerView(
            roomId = RoomId("!room:matrix.org"),
            roomName = "Room",
            roomAvatarData = null,
            memberSuggestions = persistentListOf(
                ResolvedMentionSuggestion.AtRoom,
                ResolvedMentionSuggestion.Member(roomMember),
                ResolvedMentionSuggestion.Member(roomMember.copy(userId = UserId("@bob:server.org"), displayName = "Bob")),
            ),
            onSelectSuggestion = {}
        )
    }
}
