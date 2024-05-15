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

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.roomdetails.impl.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.designsystem.components.LabelledTextField
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncActionViewDefaults
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.modifiers.clearFocusOnTap
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.ui.components.AvatarActionBottomSheet
import io.element.android.libraries.matrix.ui.components.EditableAvatarView
import io.element.android.libraries.permissions.api.PermissionsView
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RoomDetailsEditView(
    state: RoomDetailsEditState,
    onBackPressed: () -> Unit,
    onRoomEdited: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val isAvatarActionsSheetVisible = remember { mutableStateOf(false) }

    fun onAvatarClicked() {
        focusManager.clearFocus()
        isAvatarActionsSheetVisible.value = true
    }

    Scaffold(
        modifier = modifier.clearFocusOnTap(focusManager),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.screen_room_details_edit_room_title),
                        style = ElementTheme.typography.aliasScreenTitle,
                    )
                },
                navigationIcon = { BackButton(onClick = onBackPressed) },
                actions = {
                    TextButton(
                        text = stringResource(CommonStrings.action_save),
                        enabled = state.saveButtonEnabled,
                        onClick = {
                            focusManager.clearFocus()
                            state.eventSink(RoomDetailsEditEvents.Save)
                        },
                    )
                }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            EditableAvatarView(
                matrixId = state.roomId.value,
                // As per Element Web, we use the raw name for the avatar as well
                displayName = state.roomRawName,
                avatarUrl = state.roomAvatarUrl,
                avatarSize = AvatarSize.EditRoomDetails,
                onAvatarClicked = ::onAvatarClicked,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(60.dp))

            if (state.canChangeName) {
                LabelledTextField(
                    label = stringResource(id = R.string.screen_room_details_room_name_label),
                    value = state.roomRawName,
                    placeholder = stringResource(CommonStrings.common_room_name_placeholder),
                    singleLine = true,
                    onValueChange = { state.eventSink(RoomDetailsEditEvents.UpdateRoomName(it)) },
                )
            } else {
                LabelledReadOnlyField(
                    title = stringResource(R.string.screen_room_details_room_name_label),
                    value = state.roomRawName
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (state.canChangeTopic) {
                LabelledTextField(
                    label = stringResource(CommonStrings.common_topic),
                    value = state.roomTopic,
                    placeholder = stringResource(CommonStrings.common_topic_placeholder),
                    maxLines = 10,
                    onValueChange = { state.eventSink(RoomDetailsEditEvents.UpdateRoomTopic(it)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                )
            } else {
                LabelledReadOnlyField(
                    title = stringResource(R.string.screen_room_details_topic_title),
                    value = state.roomTopic
                )
            }
        }
    }

    AvatarActionBottomSheet(
        actions = state.avatarActions,
        isVisible = isAvatarActionsSheetVisible,
        onActionSelected = { state.eventSink(RoomDetailsEditEvents.HandleAvatarAction(it)) }
    )

    AsyncActionView(
        async = state.saveAction,
        progressDialog = {
            AsyncActionViewDefaults.ProgressDialog(
                progressText = stringResource(R.string.screen_room_details_updating_room),
            )
        },
        onSuccess = { onRoomEdited() },
        errorMessage = { stringResource(R.string.screen_room_details_edition_error) },
        onErrorDismiss = { state.eventSink(RoomDetailsEditEvents.CancelSaveChanges) }
    )

    PermissionsView(
        state = state.cameraPermissionState,
    )
}

@Composable
private fun LabelledReadOnlyField(
    title: String,
    value: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = MaterialTheme.colorScheme.primary,
            text = title,
        )

        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            text = value,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun RoomDetailsEditViewPreview(@PreviewParameter(RoomDetailsEditStateProvider::class) state: RoomDetailsEditState) = ElementPreview {
    RoomDetailsEditView(
        state = state,
        onBackPressed = {},
        onRoomEdited = {},
    )
}
