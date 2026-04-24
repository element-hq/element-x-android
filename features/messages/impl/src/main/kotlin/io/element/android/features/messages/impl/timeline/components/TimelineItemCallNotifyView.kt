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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.RtcNotificationState
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import io.element.android.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.matrix.api.notification.CallIntent
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun TimelineItemCallNotifyView(
    timelineRoomInfo: TimelineRoomInfo,
    event: TimelineItem.Event,
    content: TimelineItemRtcNotificationContent,
    onLongClick: (TimelineItem.Event) -> Unit,
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
        Icon(
            modifier = Modifier.size(20.sp.toDp()),
            imageVector = getIcon(timelineRoomInfo, content),
            contentDescription = null,
            tint = ElementTheme.colors.iconSecondary,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(getTextRes(timelineRoomInfo, content)),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = event.sentTime,
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun getTextRes(
    timelineRoomInfo: TimelineRoomInfo,
    content: TimelineItemRtcNotificationContent
): Int = if (timelineRoomInfo.isDm) {
    when (content.state) {
        RtcNotificationState.Declined -> CommonStrings.common_call_declined
        RtcNotificationState.DeclinedByMe -> CommonStrings.common_call_you_declined
        RtcNotificationState.None -> CommonStrings.common_call_started
    }
} else {
    // Only show declined info in DMs
    CommonStrings.common_call_started
}

@Composable
private fun getIcon(
    timelineRoomInfo: TimelineRoomInfo,
    content: TimelineItemRtcNotificationContent
): ImageVector {
    val showAsDeclined = timelineRoomInfo.isDm && (
        content.state == RtcNotificationState.Declined ||
            content.state == RtcNotificationState.DeclinedByMe
        )
    val icon = if (showAsDeclined) {
        if (content.callIntent == CallIntent.AUDIO) CompoundIcons.VoiceCallDeclinedSolid() else CompoundIcons.VideoCallDeclinedSolid()
    } else {
        if (content.callIntent == CallIntent.AUDIO) CompoundIcons.VoiceCallSolid() else CompoundIcons.VideoCallSolid()
    }
    return icon
}

@PreviewsDayNight
@Composable
internal fun TimelineItemCallNotifyViewPreview() = ElementPreview {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        listOf(
            (aTimelineRoomInfo() to TimelineItemRtcNotificationContent(CallIntent.AUDIO, RtcNotificationState.None)),
            (aTimelineRoomInfo() to TimelineItemRtcNotificationContent(CallIntent.VIDEO, RtcNotificationState.None)),
            (aTimelineRoomInfo(isDm = true) to TimelineItemRtcNotificationContent(CallIntent.AUDIO, RtcNotificationState.Declined)),
            (aTimelineRoomInfo(isDm = true) to TimelineItemRtcNotificationContent(CallIntent.VIDEO, RtcNotificationState.Declined)),
            (aTimelineRoomInfo(isDm = true) to TimelineItemRtcNotificationContent(CallIntent.VIDEO, RtcNotificationState.DeclinedByMe)),
            (aTimelineRoomInfo(isDm = false) to TimelineItemRtcNotificationContent(CallIntent.VIDEO, RtcNotificationState.None)),
        ).forEach { (info, content) ->
            TimelineItemCallNotifyView(
                timelineRoomInfo = info,
                event = aTimelineItemEvent(content = content),
                content = content,
                onLongClick = {},
            )
        }
    }
}
