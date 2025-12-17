/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.features.roomcall.api.RoomCallStateProvider
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun TimelineItemCallNotifyView(
    event: TimelineItem.Event,
    roomCallState: RoomCallState,
    onLongClick: (TimelineItem.Event) -> Unit,
    onJoinCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ElementTheme.colors.borderInteractiveSecondary, RoundedCornerShape(8.dp))
            .combinedClickable(
                enabled = true,
                onClick = {},
                onLongClick = { onLongClick(event) },
                onLongClickLabel = stringResource(CommonStrings.action_open_context_menu),
            )
            .onKeyboardContextMenuAction { onLongClick(event) }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            avatarData = event.senderAvatar,
            avatarType = AvatarType.User,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.safeSenderName,
                style = ElementTheme.typography.fontBodyLgMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(20.sp.toDp()),
                    imageVector = CompoundIcons.VideoCallSolid(),
                    contentDescription = null,
                    tint = ElementTheme.colors.iconSecondary,
                )
                Text(
                    text = stringResource(CommonStrings.common_call_started),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (roomCallState is RoomCallState.OnGoing) {
            CallMenuItem(
                roomCallState = roomCallState,
                onJoinCallClick = onJoinCallClick,
            )
        } else {
            Text(
                text = event.sentTime,
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemCallNotifyViewPreview() = ElementPreview {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RoomCallStateProvider()
            .values
            .filter { it !is RoomCallState.Unavailable }
            .forEach { roomCallState ->
                TimelineItemCallNotifyView(
                    event = aTimelineItemEvent(content = TimelineItemRtcNotificationContent()),
                    roomCallState = roomCallState,
                    onLongClick = {},
                    onJoinCallClick = {},
                )
            }
    }
}
