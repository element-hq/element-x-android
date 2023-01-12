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

package io.element.android.x.features.messages

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.x.core.compose.LogCompositions
import io.element.android.x.designsystem.components.avatar.Avatar
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.features.messages.actionlist.TimelineItemAction
import io.element.android.x.features.messages.actionlist.ActionListView
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import io.element.android.x.features.messages.textcomposer.MessageComposerView
import io.element.android.x.features.messages.timeline.TimelineView
import timber.log.Timber

@Composable
fun MessagesView(
    state: MessagesState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
) {

    LogCompositions(tag = "MessagesScreen", msg = "Root")
    val itemActionsBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
    )
    val snackbarHostState = remember { SnackbarHostState() }

    LogCompositions(tag = "MessagesScreen", msg = "Content")
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
            )
        },
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        },
    )

    fun onActionSelected(action: TimelineItemAction, messageEvent: MessagesTimelineItemState.MessageEvent) {
        state.eventSink(MessagesEvents.HandleAction(action, messageEvent))
    }

    ActionListView(
        state = state.actionListState,
        modalBottomSheetState = itemActionsBottomSheetState,
        onActionSelected = ::onActionSelected
    )
}

@Composable
fun MessagesViewContent(
    state: MessagesState,
    modifier: Modifier = Modifier
) {

    fun onMessageClicked(messageEvent: MessagesTimelineItemState.MessageEvent) {
        Timber.v("OnMessageClicked= $messageEvent")
    }

    fun onMessageLongClicked(messageEvent: MessagesTimelineItemState.MessageEvent) {
        Timber.v("OnMessageLongClicked= $messageEvent")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        if (!state.composerState.isFullScreen) {
            TimelineView(
                state = state.timelineState,
                modifier = Modifier.fillMaxWidth(),
                onMessageClicked = ::onMessageClicked,
                onMessageLongClicked = ::onMessageLongClicked
            )
        }
        MessageComposerView(
            state = state.composerState,
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    if (state.composerState.isFullScreen) {
                        it.weight(1f, fill = false)
                    } else {
                        it.wrapContentHeight(Alignment.Bottom)
                    }
                },
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
