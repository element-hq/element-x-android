/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.pinned.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.TimelineItemRow
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun PinnedMessagesListView(
    state: PinnedMessagesListState,
    onBackClick: () -> Unit,
    onEventClick: (event: TimelineItem.Event) -> Unit,
    onUserDataClick: (UserId) -> Unit,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PinnedMessagesListTopBar(state, onBackClick)
        },
        content = { padding ->
            PinnedMessagesListContent(
                state = state,
                onEventClick = onEventClick,
                onUserDataClick = onUserDataClick,
                onLinkClick = onLinkClick,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding),
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PinnedMessagesListTopBar(
    state: PinnedMessagesListState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = CommonStrings.screen_room_pinned_banner_indicator_description, state.pinnedMessagesCount),
                style = ElementTheme.typography.fontBodyLgMedium
            )
        },
        navigationIcon = { BackButton(onClick = onBackClick) },
        modifier = modifier,
    )
}

@Composable
private fun PinnedMessagesListContent(
    state: PinnedMessagesListState,
    onEventClick: (event: TimelineItem.Event) -> Unit,
    onUserDataClick: (UserId) -> Unit,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = rememberLazyListState(),
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(
                items = state.timelineItems,
                contentType = { timelineItem -> timelineItem.contentType() },
                key = { timelineItem -> timelineItem.identifier() },
            ) { timelineItem ->
                TimelineItemRow(
                    timelineItem = timelineItem,
                    timelineRoomInfo = state.timelineRoomInfo,
                    renderReadReceipts = false,
                    isLastOutgoingMessage = false,
                    focusedEventId = null,
                    onClick = onEventClick,
                    onLongClick = {},
                    onUserDataClick = onUserDataClick,
                    onLinkClick = onLinkClick,
                    inReplyToClick = {},
                    onReactionClick = { _, _ -> },
                    onReactionLongClick = { _, _ -> },
                    onMoreReactionsClick = {},
                    onReadReceiptClick = {},
                    eventSink = {},
                    onSwipeToReply = {},
                    onJoinCallClick = {},
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
fun PinnedMessagesTimelineViewPreview(@PreviewParameter(PinnedMessagesTimelineStateProvider::class) state: PinnedMessagesListState) =
    ElementPreview {
        PinnedMessagesListView(
            state = state,
            onBackClick = {},
            onEventClick = { },
            onUserDataClick = {},
            onLinkClick = {},
        )
    }
