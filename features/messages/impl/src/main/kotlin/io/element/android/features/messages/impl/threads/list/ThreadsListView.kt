/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.threads.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
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
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.atoms.UnreadIndicatorAtom
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.asEventId
import io.element.android.libraries.matrix.api.room.threads.ThreadListItem
import io.element.android.libraries.matrix.api.room.threads.ThreadListItemEvent
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.getAvatarUrl
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadsListView(
    state: ThreadsListState,
    onThreadClick: (ThreadId) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(
                            avatarData = AvatarData(
                                id = state.roomId.value,
                                name = state.roomName,
                                url = state.roomAvatarUrl,
                                size = AvatarSize.CurrentUserTopBar,
                            ),
                            avatarType = AvatarType.Room(isTombstoned = state.isRoomTombstoned),
                            contentDescription = null,
                        )
                        Column {
                            Text(
                                text = stringResource(CommonStrings.common_threads),
                                style = ElementTheme.typography.fontBodyLgMedium,
                                color = ElementTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = state.roomName,
                                style = ElementTheme.typography.fontBodyXsRegular,
                                color = ElementTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                navigationIcon = {
                    BackButton(onBackClick)
                }
            )
        }
    ) { padding ->
        val lazyListState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding,
            state = lazyListState,
        ) {
            itemsIndexed(state.threads, key = { _, row -> row.item.threadId }) { index, row ->
                ThreadListItemRow(
                    threadItem = row,
                    onClick = onThreadClick,
                )

                if (index < state.threads.size - 1) {
                    HorizontalDivider()
                }
            }
        }

        ScrollHelper(lazyListState) {
            state.eventSink(ThreadsListEvents.Paginate)
        }
    }
}

@Composable
private fun ScrollHelper(
    listState: LazyListState,
    onPaginate: () -> Unit,
) {
    val lastVisibleItemIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size - 1 }
    }
    val needsPagination by remember {
        derivedStateOf {
            val canLoadNewItems = listState.isScrollInProgress || listState.firstVisibleItemScrollOffset == 0
            canLoadNewItems && lastVisibleItemIndex == listState.layoutInfo.totalItemsCount - 1
        }
    }
    LaunchedEffect(needsPagination, lastVisibleItemIndex) {
        if (needsPagination) {
            onPaginate()
            delay(400L)
        }
    }
}

@Composable
private fun ThreadListItemRow(
    threadItem: ThreadListRowItem,
    onClick: (ThreadId) -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable { onClick(threadItem.item.threadId) }
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
    ) {
        val rootEvent = threadItem.item.rootEvent
        val senderProfile = rootEvent.senderProfile
        Avatar(
            modifier = Modifier.align(Alignment.CenterVertically),
            avatarData = AvatarData(
                id = rootEvent.senderId.value,
                name = senderProfile.getDisambiguatedDisplayName(rootEvent.senderId),
                url = senderProfile.getAvatarUrl(),
                size = AvatarSize.ThreadsListItem,
            ),
            avatarType = AvatarType.User,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            // TODO actually compute these values based on the thread state (not available yet)
            val hasMentions = false
            val hasUnreadNotifications = false

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = senderProfile.getDisambiguatedDisplayName(rootEvent.senderId),
                    style = ElementTheme.typography.fontBodyLgMedium,
                    color = ElementTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = threadItem.formattedTimestamp,
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = if (hasUnreadNotifications || hasMentions) ElementTheme.colors.textActionAccent else ElementTheme.colors.textSecondary,
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = threadItem.rootEventText.orEmpty(),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    if (hasMentions) {
                        Icon(
                            modifier = Modifier.size(14.dp),
                            imageVector = CompoundIcons.Mention(),
                            contentDescription = null,
                            tint = ElementTheme.colors.textActionAccent,
                        )
                    }

                    UnreadIndicatorAtom(
                        size = 14.dp,
                        isVisible = hasUnreadNotifications,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${threadItem.item.numberOfReplies}",
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = CompoundIcons.ThreadsSolid(),
                    contentDescription = null,
                    tint = ElementTheme.colors.iconSecondary
                )

                Spacer(modifier = Modifier.width(8.dp))

                threadItem.item.latestEvent?.let { latestEvent ->
                    Avatar(
                        avatarData = AvatarData(
                            id = latestEvent.senderId.value,
                            name = latestEvent.senderProfile.getDisambiguatedDisplayName(latestEvent.senderId),
                            url = latestEvent.senderProfile.getAvatarUrl(),
                            size = AvatarSize.TimelineThreadLatestEventSender,
                        ),
                        avatarType = AvatarType.User,
                        contentDescription = null,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = threadItem.latestEventText.orEmpty(),
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ThreadsListViewPreview() {
    ElementPreview {
        ThreadsListView(
            state = ThreadsListState(
                roomId = RoomId("!room-id:server"),
                roomName = "Room name",
                roomAvatarUrl = null,
                threads = List(10) { aThreadListRowItem(threadId = ThreadId("\$thread-$it")) }.toImmutableList(),
                isRoomTombstoned = false,
                eventSink = {},
            ),
            onThreadClick = {},
            onBackClick = {},
        )
    }
}

@PreviewsDayNight
@Composable
internal fun ThreadListItemRowPreview() {
    ElementPreview {
        ThreadListItemRow(
            threadItem = aThreadListRowItem(),
            onClick = {},
        )
    }
}

fun aThreadListRowItem(
    threadId: ThreadId = ThreadId("\$a-thread-id"),
    rootEvent: ThreadListItemEvent = aThreadListItemEvent(threadId = threadId),
    latestEvent: ThreadListItemEvent? = aThreadListItemEvent(threadId = threadId),
    numberOfReplies: Long = 42,
    rootEventText: String? = "Hello world!",
    latestEventText: String? = "Hello again!",
    formattedTimestamp: String = "12:34",
) = ThreadListRowItem(
    item = aThreadListItem(
        threadId = threadId,
        rootEvent = rootEvent,
        latestEvent = latestEvent,
        numberOfReplies = numberOfReplies,
    ),
    rootEventText = rootEventText,
    latestEventText = latestEventText,
    formattedTimestamp = formattedTimestamp,
)

fun aThreadListItem(
    threadId: ThreadId = ThreadId("\$a-thread-id"),
    rootEvent: ThreadListItemEvent = aThreadListItemEvent(threadId = threadId),
    latestEvent: ThreadListItemEvent? = aThreadListItemEvent(threadId = threadId),
    numberOfReplies: Long = 42,
) = ThreadListItem(
    rootEvent = rootEvent,
    latestEvent = latestEvent,
    numberOfReplies = numberOfReplies,
)

fun aThreadListItemEvent(
    threadId: ThreadId = ThreadId("\$a-thread-id"),
    senderId: UserId = UserId("@a-user-id:server"),
    senderProfile: ProfileDetails = ProfileDetails.Ready(displayName = "Alice", displayNameAmbiguous = false, avatarUrl = null),
    isOwn: Boolean = false,
    content: EventContent = MessageContent(
        body = "Hello world!",
        inReplyTo = null,
        isEdited = false,
        threadInfo = null,
        type = TextMessageType("Hello world!", null),
    ),
    timestamp: Long = 0L,
) = ThreadListItemEvent(
    eventId = threadId.asEventId(),
    senderId = senderId,
    senderProfile = senderProfile,
    isOwn = isOwn,
    content = content,
    timestamp = timestamp,
)
