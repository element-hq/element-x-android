/*
 * Copyright (c) 2022 New Vector Ltd
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

@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
)

package io.element.android.features.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.messages.actionlist.ActionListEvents
import io.element.android.features.messages.actionlist.ActionListView
import io.element.android.features.messages.actionlist.anActionListState
import io.element.android.features.messages.actionlist.model.TimelineItemAction
import io.element.android.features.messages.textcomposer.MessageComposerView
import io.element.android.features.messages.textcomposer.aMessageComposerState
import io.element.android.features.messages.timeline.TimelineView
import io.element.android.features.messages.timeline.aTimelineState
import io.element.android.features.messages.timeline.createTimelineItemContent
import io.element.android.features.messages.timeline.createTimelineItems
import io.element.android.features.messages.timeline.model.TimelineItem
import io.element.android.libraries.core.data.StableCharSequence
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.LogCompositions
import io.element.android.libraries.matrix.core.RoomId
import io.element.android.libraries.textcomposer.MessageComposerMode
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun MessagesView(
    state: MessagesState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
) {
    LogCompositions(tag = "MessagesScreen", msg = "Root")
    val itemActionsBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    LogCompositions(tag = "MessagesScreen", msg = "Content")

    fun onMessageClicked(messageEvent: TimelineItem.MessageEvent) {
        Timber.v("OnMessageClicked= ${messageEvent.id}")
    }

    fun onMessageLongClicked(messageEvent: TimelineItem.MessageEvent) {
        Timber.v("OnMessageLongClicked= ${messageEvent.id}")
        focusManager.clearFocus(force = true)
        state.actionListState.eventSink(ActionListEvents.ComputeForMessage(messageEvent))
        coroutineScope.launch {
            itemActionsBottomSheetState.show()
        }
    }

    fun onActionSelected(action: TimelineItemAction, messageEvent: TimelineItem.MessageEvent) {
        state.eventSink(MessagesEvents.HandleAction(action, messageEvent))
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            MessagesViewTopBar(
                roomTitle = state.roomName,
                roomAvatar = state.roomAvatar,
                onBackPressed = onBackPressed
            )
        },
        content = { padding ->
            MessagesViewContent(
                state = state,
                modifier = Modifier.padding(padding),
                onMessageClicked = ::onMessageClicked,
                onMessageLongClicked = ::onMessageLongClicked
            )
        },
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        },
    )

    ActionListView(
        state = state.actionListState,
        modalBottomSheetState = itemActionsBottomSheetState,
        onActionSelected = ::onActionSelected
    )
}

@Composable
fun MessagesViewContent(
    state: MessagesState,
    modifier: Modifier = Modifier,
    onMessageClicked: (TimelineItem.MessageEvent) -> Unit = {},
    onMessageLongClicked: (TimelineItem.MessageEvent) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Hide timeline if composer is full screen
        if (!state.composerState.isFullScreen) {
            TimelineView(
                state = state.timelineState,
                modifier = Modifier.weight(1f),
                onMessageClicked = onMessageClicked,
                onMessageLongClicked = onMessageLongClicked
            )
        }
        MessageComposerView(
            state = state.composerState,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Bottom)
        )
    }
}

@Composable
fun MessagesViewTopBar(
    roomTitle: String?,
    roomAvatar: AvatarData?,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (roomAvatar != null) {
                    Avatar(roomAvatar)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    text = roomTitle ?: "Unknown room",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    )
}

@Preview
@Composable
fun MessagesViewLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
fun MessagesViewDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    MessagesView(
        MessagesState(
            roomId = RoomId(""),
            roomName = "Room name",
            roomAvatar = AvatarData("Room name"),
            composerState = aMessageComposerState().copy(
                text = StableCharSequence("Hello"),
                isFullScreen = false,
                mode = MessageComposerMode.Normal("Hello"),
            ),
            timelineState = aTimelineState().copy(
                timelineItems = createTimelineItems(createTimelineItemContent()),
                hasMoreToLoad = false,
            ),
            actionListState = anActionListState(),
            eventSink = {}
        )
    )
}
