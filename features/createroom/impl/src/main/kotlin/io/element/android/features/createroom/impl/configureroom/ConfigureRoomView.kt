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

package io.element.android.features.createroom.impl.configureroom

import android.net.Uri
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.createroom.impl.R
import io.element.android.features.createroom.impl.components.Avatar
import io.element.android.features.createroom.impl.components.LabelledTextField
import io.element.android.features.createroom.impl.components.RoomPrivacyOption
import io.element.android.features.createroom.impl.configureroom.avatar.AvatarActionListView
import io.element.android.features.userlist.api.components.SelectedUsersList
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.CenterAlignedTopAppBar
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.launch
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ConfigureRoomView(
    state: ConfigureRoomState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onRoomCreated: (RoomId) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val itemActionsBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
    )

    if (state.createRoomAction is Async.Success) {
        LaunchedEffect(state.createRoomAction) {
            onRoomCreated(state.createRoomAction.state)
        }
    }

    fun onAvatarClicked() {
        focusManager.clearFocus()
        coroutineScope.launch {
            itemActionsBottomSheetState.show()
        }
    }

    Scaffold(
        modifier = modifier.clearFocusOnTap(focusManager),
        topBar = {
            ConfigureRoomToolbar(
                isNextActionEnabled = state.isCreateButtonEnabled,
                onBackPressed = onBackPressed,
                onNextPressed = {
                    focusManager.clearFocus()
                    state.eventSink(ConfigureRoomEvents.CreateRoom(state.config))
                },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                RoomNameWithAvatar(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    avatarUri = state.config.avatarUri,
                    roomName = state.config.roomName.orEmpty(),
                    onAvatarClick = ::onAvatarClicked,
                    onRoomNameChanged = { state.eventSink(ConfigureRoomEvents.RoomNameChanged(it)) },
                )
            }
            item {
                RoomTopic(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    topic = state.config.topic.orEmpty(),
                    onTopicChanged = { state.eventSink(ConfigureRoomEvents.TopicChanged(it)) },
                )
            }
            if (state.config.invites.isNotEmpty()) {
                item {
                    SelectedUsersList(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        selectedUsers = state.config.invites,
                        onUserRemoved = {
                            focusManager.clearFocus()
                            state.eventSink(ConfigureRoomEvents.RemoveFromSelection(it))
                        },
                    )
                }
            }
            item {
                RoomPrivacyOptions(
                    modifier = Modifier.padding(bottom = 40.dp),
                    selected = state.config.privacy,
                    onOptionSelected = {
                        focusManager.clearFocus()
                        state.eventSink(ConfigureRoomEvents.RoomPrivacyChanged(it.privacy))
                    },
                )
            }
        }
    }

    AvatarActionListView(
        actions = state.avatarActions,
        modalBottomSheetState = itemActionsBottomSheetState,
        onActionSelected = { state.eventSink(ConfigureRoomEvents.HandleAvatarAction(it)) }
    )

    when (state.createRoomAction) {
        is Async.Loading -> {
            ProgressDialog(text = stringResource(StringR.string.common_creating_room))
        }

        is Async.Failure -> {
            RetryDialog(
                content = stringResource(R.string.screen_create_room_error_creating_room),
                onDismiss = { state.eventSink(ConfigureRoomEvents.CancelCreateRoom) },
                onRetry = { state.eventSink(ConfigureRoomEvents.CreateRoom(state.config)) },
            )
        }

        else -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureRoomToolbar(
    isNextActionEnabled: Boolean,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onNextPressed: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.screen_create_room_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = { BackButton(onClick = onBackPressed) },
        actions = {
            TextButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                enabled = isNextActionEnabled,
                onClick = onNextPressed,
            ) {
                Text(
                    text = stringResource(StringR.string.action_create),
                    fontSize = 16.sp,
                )
            }
        }
    )
}

@Composable
fun RoomNameWithAvatar(
    avatarUri: Uri?,
    roomName: String,
    modifier: Modifier = Modifier,
    onAvatarClick: () -> Unit = {},
    onRoomNameChanged: (String) -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            avatarUri = avatarUri,
            onClick = onAvatarClick,
        )

        LabelledTextField(
            label = stringResource(R.string.screen_create_room_room_name_label),
            value = roomName,
            placeholder = stringResource(R.string.screen_create_room_room_name_placeholder),
            onValueChange = onRoomNameChanged
        )
    }
}

@Composable
fun RoomTopic(
    topic: String,
    modifier: Modifier = Modifier,
    onTopicChanged: (String) -> Unit = {},
) {
    LabelledTextField(
        modifier = modifier,
        label = stringResource(R.string.screen_create_room_topic_label),
        value = topic,
        placeholder = stringResource(R.string.screen_create_room_topic_placeholder),
        onValueChange = onTopicChanged,
        maxLines = 3,
    )
}

@Composable
fun RoomPrivacyOptions(
    selected: RoomPrivacy?,
    modifier: Modifier = Modifier,
    onOptionSelected: (RoomPrivacyItem) -> Unit = {},
) {
    val items = roomPrivacyItems()
    Column(modifier = modifier.selectableGroup()) {
        items.forEach { item ->
            RoomPrivacyOption(
                roomPrivacyItem = item,
                isSelected = selected == item.privacy,
                onOptionSelected = onOptionSelected,
            )
        }
    }
}

@Preview
@Composable
fun ConfigureRoomViewLightPreview(@PreviewParameter(ConfigureRoomStateProvider::class) state: ConfigureRoomState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun ConfigureRoomViewDarkPreview(@PreviewParameter(ConfigureRoomStateProvider::class) state: ConfigureRoomState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: ConfigureRoomState) {
    ConfigureRoomView(
        state = state,
    )
}

private fun Modifier.clearFocusOnTap(focusManager: FocusManager) = composed {
    pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
        })
    }
}
