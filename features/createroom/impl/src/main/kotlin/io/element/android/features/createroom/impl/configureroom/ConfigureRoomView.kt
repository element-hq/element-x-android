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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.createroom.impl.R
import io.element.android.features.createroom.impl.components.RoomPrivacyOption
import io.element.android.libraries.designsystem.components.LabelledTextField
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncActionViewDefaults
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.modifiers.clearFocusOnTap
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.components.AvatarActionBottomSheet
import io.element.android.libraries.matrix.ui.components.SelectedUsersList
import io.element.android.libraries.matrix.ui.components.UnsavedAvatar
import io.element.android.libraries.permissions.api.PermissionsView
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ConfigureRoomView(
    state: ConfigureRoomState,
    onBackPressed: () -> Unit,
    onRoomCreated: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val itemActionsBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
    )

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
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .consumeWindowInsets(padding),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            RoomNameWithAvatar(
                modifier = Modifier.padding(horizontal = 16.dp),
                avatarUri = state.config.avatarUri,
                roomName = state.config.roomName.orEmpty(),
                onAvatarClick = ::onAvatarClicked,
                onRoomNameChanged = { state.eventSink(ConfigureRoomEvents.RoomNameChanged(it)) },
            )
            RoomTopic(
                modifier = Modifier.padding(horizontal = 16.dp),
                topic = state.config.topic.orEmpty(),
                onTopicChanged = { state.eventSink(ConfigureRoomEvents.TopicChanged(it)) },
            )
            if (state.config.invites.isNotEmpty()) {
                SelectedUsersList(
                    modifier = Modifier.padding(bottom = 16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    selectedUsers = state.config.invites,
                    onUserRemoved = {
                        focusManager.clearFocus()
                        state.eventSink(ConfigureRoomEvents.RemoveFromSelection(it))
                    },
                )
            }
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

    AvatarActionBottomSheet(
        actions = state.avatarActions,
        modalBottomSheetState = itemActionsBottomSheetState,
        onActionSelected = { state.eventSink(ConfigureRoomEvents.HandleAvatarAction(it)) }
    )

    AsyncActionView(
        async = state.createRoomAction,
        progressDialog = {
            AsyncActionViewDefaults.ProgressDialog(
                progressText = stringResource(CommonStrings.common_creating_room),
            )
        },
        onSuccess = { onRoomCreated(it) },
        errorMessage = { stringResource(R.string.screen_create_room_error_creating_room) },
        onRetry = { state.eventSink(ConfigureRoomEvents.CreateRoom(state.config)) },
        onErrorDismiss = { state.eventSink(ConfigureRoomEvents.CancelCreateRoom) },
    )

    PermissionsView(
        state = state.cameraPermissionState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigureRoomToolbar(
    isNextActionEnabled: Boolean,
    onBackPressed: () -> Unit,
    onNextPressed: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.screen_create_room_title),
                style = ElementTheme.typography.aliasScreenTitle,
            )
        },
        navigationIcon = { BackButton(onClick = onBackPressed) },
        actions = {
            TextButton(
                text = stringResource(CommonStrings.action_create),
                enabled = isNextActionEnabled,
                onClick = onNextPressed,
            )
        }
    )
}

@Composable
private fun RoomNameWithAvatar(
    avatarUri: Uri?,
    roomName: String,
    onAvatarClick: () -> Unit,
    onRoomNameChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UnsavedAvatar(
            avatarUri = avatarUri,
            modifier = Modifier.clickable(onClick = onAvatarClick),
        )

        LabelledTextField(
            label = stringResource(R.string.screen_create_room_room_name_label),
            value = roomName,
            placeholder = stringResource(CommonStrings.common_room_name_placeholder),
            singleLine = true,
            onValueChange = onRoomNameChanged,
        )
    }
}

@Composable
private fun RoomTopic(
    topic: String,
    onTopicChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LabelledTextField(
        modifier = modifier,
        label = stringResource(R.string.screen_create_room_topic_label),
        value = topic,
        placeholder = stringResource(CommonStrings.common_topic_placeholder),
        onValueChange = onTopicChanged,
        maxLines = 3,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
        ),
    )
}

@Composable
private fun RoomPrivacyOptions(
    selected: RoomPrivacy?,
    onOptionSelected: (RoomPrivacyItem) -> Unit,
    modifier: Modifier = Modifier,
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

@PreviewsDayNight
@Composable
internal fun ConfigureRoomViewPreview(@PreviewParameter(ConfigureRoomStateProvider::class) state: ConfigureRoomState) = ElementPreview {
    ConfigureRoomView(
        state = state,
        onBackPressed = {},
        onRoomCreated = {},
    )
}
