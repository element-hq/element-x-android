/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.ExperimentalFoundationApi
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
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemCallNotifyContent
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TimelineItemCallNotifyView(
    event: TimelineItem.Event,
    isCallOngoing: Boolean,
    onLongClick: (TimelineItem.Event) -> Unit,
    onJoinCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ElementTheme.colors.borderInteractiveSecondary, RoundedCornerShape(8.dp))
            .combinedClickable(enabled = true, onClick = {}, onLongClick = { onLongClick(event) })
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(avatarData = event.senderAvatar)
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
        if (isCallOngoing) {
            JoinCallMenuItem(onJoinCallClick)
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
internal fun TimelineItemCallNotifyViewPreview() {
    ElementPreview {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TimelineItemCallNotifyView(
                event = aTimelineItemEvent(content = TimelineItemCallNotifyContent()),
                isCallOngoing = true,
                onLongClick = {},
                onJoinCallClick = {},
            )
            TimelineItemCallNotifyView(
                event = aTimelineItemEvent(content = TimelineItemCallNotifyContent()),
                isCallOngoing = false,
                onLongClick = {},
                onJoinCallClick = {},
            )
        }
    }
}
