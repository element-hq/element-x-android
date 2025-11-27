/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.threads.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelineState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemThreadInfo
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.getAvatarUrl
import io.element.android.libraries.matrix.api.timeline.item.event.getDisplayName

@Composable
fun ThreadListView(
    state: TimelineState,
    modifier: Modifier = Modifier,
    onThreadClick: (EventId) -> Unit,
) {
    val lazyListState = rememberLazyListState()

    if (state.timelineItems.isEmpty() && !state.paginationState.isPaginating && state.paginationState.hasReachedEnd) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(id = R.string.screen_room_thread_list_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            items = state.timelineItems
                .filterIsInstance<TimelineItem.Event>()
                .filter { it.threadInfo is TimelineItemThreadInfo.ThreadRoot },
            key = { it.id }
        ) { timelineItem ->
            ThreadListRow(
                event = timelineItem,
                onClick = {
                    val eventId = timelineItem.eventId ?: return@ThreadListRow
                    onThreadClick(eventId)
                }
            )
        }
        if (state.paginationState.isPaginating) {
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    val shouldPaginate by remember {
        derivedStateOf {
            val lastVisibleItemIndex = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            !state.paginationState.isPaginating &&
                !state.paginationState.hasReachedEnd &&
                lastVisibleItemIndex != -1 &&
                lastVisibleItemIndex >= state.timelineItems.size - 5
        }
    }

    LaunchedEffect(shouldPaginate) {
        if (shouldPaginate) {
            state.eventSink(TimelineEvents.LoadMore(Timeline.PaginationDirection.BACKWARDS))
        }
    }
}

@Composable
fun ThreadListRow(
    event: TimelineItem.Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Col 1: Avatar
        Avatar(
            avatarData = event.senderAvatar.copy(size = AvatarSize.AccountItem),
            avatarType = AvatarType.User,
        )
        Spacer(Modifier.width(12.dp))

        // Col 2:
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.safeSenderName,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(2.dp))
            val content = event.content as? TimelineItemTextBasedContent
            if (content != null) {
                Text(
                    text = content.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val threadInfo = event.threadInfo as? TimelineItemThreadInfo.ThreadRoot
            if (threadInfo != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${threadInfo.summary.numberOfReplies}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    threadInfo.summary.latestEvent.dataOrNull()?.let {
                        val avatarData = AvatarData(
                            id = it.senderId.value,
                            name = it.senderProfile.getDisplayName(),
                            url = it.senderProfile.getAvatarUrl(),
                            size = AvatarSize.TimelineThreadLatestEventSender,
                        )
                        Avatar(
                            avatarData = avatarData,
                            avatarType = AvatarType.User,
                        )
                    }
                    Text(
                        text = threadInfo.latestEventText.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Spacer(Modifier.width(12.dp))

        // Col 3: Date
        Text(
            text = event.sentTime,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
