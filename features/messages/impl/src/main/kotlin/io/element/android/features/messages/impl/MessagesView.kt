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

package io.element.android.features.messages.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.messages.impl.actionlist.ActionListEvents
import io.element.android.features.messages.impl.actionlist.ActionListView
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.messagecomposer.AttachmentsState
import io.element.android.features.messages.impl.messagecomposer.MessageComposerEvents
import io.element.android.features.messages.impl.messagecomposer.MessageComposerView
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelineView
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.networkmonitor.api.ui.ConnectivityIndicatorView
import io.element.android.libraries.androidutils.ui.hideKeyboard
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheetLayout
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.LogCompositions
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import timber.log.Timber
import io.element.android.libraries.ui.strings.R as StringsR

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun MessagesView(
    state: MessagesState,
    onBackPressed: () -> Unit,
    onRoomDetailsClicked: () -> Unit,
    onEventClicked: (event: TimelineItem.Event) -> Unit,
    onUserDataClicked: (UserId) -> Unit,
    onPreviewAttachments: (ImmutableList<Attachment>) -> Unit,
    modifier: Modifier = Modifier,
) {
    LogCompositions(tag = "MessagesScreen", msg = "Root")
    val itemActionsBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
    )
    val composerState = state.composerState
    val initialBottomSheetState = if (LocalInspectionMode.current && composerState.showAttachmentSourcePicker) {
        ModalBottomSheetValue.Expanded
    } else {
        ModalBottomSheetValue.Hidden
    }
    val bottomSheetState = rememberModalBottomSheetState(initialValue = initialBottomSheetState)
    val coroutineScope = rememberCoroutineScope()

    AttachmentStateView(state.composerState.attachmentsState, onPreviewAttachments)

    BackHandler(enabled = bottomSheetState.isVisible) {
        coroutineScope.launch {
            bottomSheetState.hide()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessageText = state.snackbarMessage?.let { stringResource(it.messageResId) }
    if (snackbarMessageText != null) {
        SideEffect {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = snackbarMessageText,
                    duration = state.snackbarMessage.duration
                )
            }
        }
    }

    // This is needed because the composer is inside an AndroidView that can't be affected by the FocusManager in Compose
    val localView = LocalView.current

    LogCompositions(tag = "MessagesScreen", msg = "Content")

    fun onMessageClicked(event: TimelineItem.Event) {
        Timber.v("OnMessageClicked= ${event.id}")
        onEventClicked(event)
    }

    fun onMessageLongClicked(event: TimelineItem.Event) {
        Timber.v("OnMessageLongClicked= ${event.id}")
        localView.hideKeyboard()
        state.actionListState.eventSink(ActionListEvents.ComputeForMessage(event))
        coroutineScope.launch {
            itemActionsBottomSheetState.show()
        }
    }

    fun onExpandGroupClick(event: TimelineItem.GroupedEvents) {
        Timber.v("onExpandGroupClick= ${event.id}")
        state.timelineState.eventSink(TimelineEvents.ToggleExpandGroup(event))
    }

    fun onActionSelected(action: TimelineItemAction, event: TimelineItem.Event) {
        state.eventSink(MessagesEvents.HandleAction(action, event))
    }

    LaunchedEffect(composerState.showAttachmentSourcePicker) {
        if (composerState.showAttachmentSourcePicker) {
            // We need to use this instead of `LocalFocusManager.clearFocus()` to hide the keyboard when focus is on an Android View
            localView.hideKeyboard()
            bottomSheetState.show()
        } else {
            bottomSheetState.hide()
        }
    }
    // Send 'DismissAttachmentMenu' event when the bottomsheet was just hidden
    LaunchedEffect(bottomSheetState.isVisible) {
        if (!bottomSheetState.isVisible) {
            composerState.eventSink(MessageComposerEvents.DismissAttachmentMenu)
        }
    }
    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        displayHandle = true,
        sheetContent = {
            AttachmentSourcePickerMenu(
                eventSink = composerState.eventSink
            )
        }
    ) {
        Scaffold(
            modifier = modifier,
            contentWindowInsets = WindowInsets.statusBars,
            topBar = {
                Column {
                    ConnectivityIndicatorView(isOnline = state.hasNetworkConnection)
                    MessagesViewTopBar(
                        roomTitle = state.roomName,
                        roomAvatar = state.roomAvatar,
                        onBackPressed = onBackPressed,
                        onRoomDetailsClicked = onRoomDetailsClicked,
                    )
                }
            },
            content = { padding ->
                MessagesViewContent(
                    state = state,
                    modifier = Modifier
                        .padding(padding)
                        .consumeWindowInsets(padding),
                    onMessageClicked = ::onMessageClicked,
                    onMessageLongClicked = ::onMessageLongClicked,
                    onExpandGroupClick = ::onExpandGroupClick,
                    onUserDataClicked = onUserDataClicked,
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
}

@Composable
private fun AttachmentStateView(
    state: AttachmentsState,
    onPreviewAttachments: (ImmutableList<Attachment>) -> Unit
) {
    when (state) {
        AttachmentsState.None -> Unit
        is AttachmentsState.Previewing -> LaunchedEffect(state) {
            onPreviewAttachments(state.attachments)
        }
        is AttachmentsState.Sending -> ProgressDialog(text = stringResource(id = StringsR.string.common_loading))
    }
}

@Composable
fun MessagesViewContent(
    state: MessagesState,
    modifier: Modifier = Modifier,
    onMessageClicked: (TimelineItem.Event) -> Unit = {},
    onUserDataClicked: (UserId) -> Unit = {},
    onMessageLongClicked: (TimelineItem.Event) -> Unit = {},
    onExpandGroupClick: (TimelineItem.GroupedEvents) -> Unit = {},
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
                onMessageLongClicked = onMessageLongClicked,
                onExpandGroupClick = onExpandGroupClick,
                onUserDataClicked = onUserDataClicked,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesViewTopBar(
    roomTitle: String?,
    roomAvatar: AvatarData?,
    modifier: Modifier = Modifier,
    onRoomDetailsClicked: () -> Unit = {},
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
            Row(
                modifier = Modifier.clickable { onRoomDetailsClicked() },
                verticalAlignment = Alignment.CenterVertically
            ) {
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
        },
        windowInsets = WindowInsets(0.dp)
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun AttachmentSourcePickerMenu(
    eventSink: (MessageComposerEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        ListItem(
            modifier = Modifier.clickable { eventSink(MessageComposerEvents.PickAttachmentSource.FromGallery) },
            icon = { Icon(Icons.Default.Collections, null) },
            text = { Text(stringResource(R.string.screen_room_attachment_source_gallery)) },
        )
        ListItem(
            modifier = Modifier.clickable { eventSink(MessageComposerEvents.PickAttachmentSource.FromFiles) },
            icon = { Icon(Icons.Default.AttachFile, null) },
            text = { Text(stringResource(R.string.screen_room_attachment_source_files)) },
        )
        ListItem(
            modifier = Modifier.clickable { eventSink(MessageComposerEvents.PickAttachmentSource.PhotoFromCamera) },
            icon = { Icon(Icons.Default.PhotoCamera, null) },
            text = { Text(stringResource(R.string.screen_room_attachment_source_camera_photo)) },
        )
        ListItem(
            modifier = Modifier.clickable { eventSink(MessageComposerEvents.PickAttachmentSource.VideoFromCamera) },
            icon = { Icon(Icons.Default.Videocam, null) },
            text = { Text(stringResource(R.string.screen_room_attachment_source_camera_video)) },
        )
    }
}

@Preview
@Composable
internal fun MessagesViewLightPreview(@PreviewParameter(MessagesStateProvider::class) state: MessagesState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun MessagesViewDarkPreview(@PreviewParameter(MessagesStateProvider::class) state: MessagesState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: MessagesState) {
    MessagesView(
        state = state,
        onBackPressed = {},
        onRoomDetailsClicked = {},
        onEventClicked = {},
        onPreviewAttachments = {},
        onUserDataClicked = {},
    )
}
