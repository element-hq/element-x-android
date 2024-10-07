/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer.suggestions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.getBestName
import io.element.android.libraries.matrix.ui.components.aRoomSummaryDetails
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.textcomposer.mentions.ResolvedSuggestion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun SuggestionsPickerView(
    isDebugBuild: Boolean,
    roomId: RoomId,
    roomName: String?,
    roomAvatarData: AvatarData?,
    suggestions: ImmutableList<ResolvedSuggestion>,
    onSelectSuggestion: (ResolvedSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            suggestions,
            key = { suggestion ->
                when (suggestion) {
                    is ResolvedSuggestion.AtRoom -> "@room"
                    is ResolvedSuggestion.Member -> suggestion.roomMember.userId.value
                    is ResolvedSuggestion.Alias -> suggestion.roomSummary.roomId.value
                }
            }
        ) {
            Column(modifier = Modifier.fillParentMaxWidth()) {
                SuggestionItemView(
                    isDebugBuild = isDebugBuild,
                    suggestion = it,
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
private fun SuggestionItemView(
    isDebugBuild: Boolean,
    suggestion: ResolvedSuggestion,
    roomId: String,
    roomName: String?,
    roomAvatar: AvatarData?,
    onSelectSuggestion: (ResolvedSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clickable { onSelectSuggestion(suggestion) },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val avatarSize = AvatarSize.Suggestion
        val avatarData = when (suggestion) {
            is ResolvedSuggestion.AtRoom -> roomAvatar?.copy(size = avatarSize) ?: AvatarData(roomId, roomName, null, avatarSize)
            is ResolvedSuggestion.Member -> suggestion.roomMember.getAvatarData(avatarSize)
            is ResolvedSuggestion.Alias -> suggestion.roomSummary.getAvatarData(avatarSize)
        }
        val title = when (suggestion) {
            is ResolvedSuggestion.AtRoom -> stringResource(R.string.screen_room_mentions_at_room_title)
            is ResolvedSuggestion.Member -> suggestion.roomMember.getBestName() // TCHAP TODO should be applied in Element X
            is ResolvedSuggestion.Alias -> suggestion.roomSummary.name
        }
        val subtitle = when (suggestion) {
            is ResolvedSuggestion.AtRoom -> "@room"
            is ResolvedSuggestion.Member -> suggestion.roomMember.userId.value.takeIf { isDebugBuild } // TCHAP hide the Matrix Id in release mode
            is ResolvedSuggestion.Alias -> suggestion.roomAlias.value
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
            subtitle?.let {
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
}

@PreviewsDayNight
@Composable
internal fun SuggestionsPickerViewPreview() {
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
        val anAlias = remember { RoomAlias("#room:domain.org") }
        val roomSummaryDetails = remember {
            aRoomSummaryDetails(
                name = "My room",
            )
        }
        SuggestionsPickerView(
            isDebugBuild = false,
            roomId = RoomId("!room:matrix.org"),
            roomName = "Room",
            roomAvatarData = null,
            suggestions = persistentListOf(
                ResolvedSuggestion.AtRoom,
                ResolvedSuggestion.Member(roomMember),
                ResolvedSuggestion.Member(roomMember.copy(userId = UserId("@bob:server.org"), displayName = "Bob")),
                ResolvedSuggestion.Alias(
                    anAlias,
                    roomSummaryDetails,
                )
            ),
            onSelectSuggestion = {}
        )
    }
}
