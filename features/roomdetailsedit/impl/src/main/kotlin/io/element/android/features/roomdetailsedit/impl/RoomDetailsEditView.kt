/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.roomdetailsedit.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncActionViewDefaults
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.SaveChangesDialog
import io.element.android.libraries.designsystem.modifiers.clearFocusOnTap
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.ui.components.AvatarActionBottomSheet
import io.element.android.libraries.matrix.ui.components.EditableAvatarView
import io.element.android.libraries.permissions.api.PermissionsView
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RoomDetailsEditView(
    state: RoomDetailsEditState,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val isAvatarActionsSheetVisible = remember { mutableStateOf(false) }

    fun onAvatarClick() {
        focusManager.clearFocus()
        isAvatarActionsSheetVisible.value = true
    }

    BackHandler {
        state.eventSink(RoomDetailsEditEvent.OnBackPress)
    }
    Scaffold(
        modifier = modifier.clearFocusOnTap(focusManager),
        topBar = {
            TopAppBar(
                titleStr = stringResource(id = R.string.screen_room_details_edit_room_title),
                navigationIcon = {
                    BackButton(
                        onClick = {
                            state.eventSink(RoomDetailsEditEvent.OnBackPress)
                        }
                    )
                },
                actions = {
                    TextButton(
                        text = stringResource(CommonStrings.action_save),
                        enabled = state.saveButtonEnabled,
                        onClick = {
                            focusManager.clearFocus()
                            state.eventSink(RoomDetailsEditEvent.Save)
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
                avatarType = if (state.isSpace) {
                    AvatarType.Space()
                } else {
                    AvatarType.Room()
                },
                enabled = state.canChangeAvatar,
                onAvatarClick = ::onAvatarClick,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                label = stringResource(id = CommonStrings.common_name),
                value = state.roomRawName,
                placeholder = stringResource(CommonStrings.common_room_name_placeholder),
                singleLine = true,
                readOnly = !state.canChangeName,
                onValueChange = { state.eventSink(RoomDetailsEditEvent.UpdateRoomName(it)) },
            )

            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                label = stringResource(CommonStrings.common_topic),
                value = state.roomTopic,
                placeholder = if (state.isSpace) {
                    stringResource(CommonStrings.common_space_topic_placeholder)
                } else {
                    stringResource(CommonStrings.common_topic_placeholder)
                },
                maxLines = 10,
                readOnly = !state.canChangeTopic,
                onValueChange = { state.eventSink(RoomDetailsEditEvent.UpdateRoomTopic(it)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
            )
        }
    }
    AvatarActionBottomSheet(
        actions = state.avatarActions,
        isVisible = isAvatarActionsSheetVisible.value,
        onDismiss = { isAvatarActionsSheetVisible.value = false },
        onSelectAction = { state.eventSink(RoomDetailsEditEvent.HandleAvatarAction(it)) }
    )
    AsyncActionView(
        async = state.saveAction,
        progressDialog = {
            AsyncActionViewDefaults.ProgressDialog(
                progressText = stringResource(R.string.screen_room_details_updating_room),
            )
        },
        confirmationDialog = {
            if (state.saveAction == AsyncAction.ConfirmingCancellation) {
                SaveChangesDialog(
                    onSaveClick = { state.eventSink(RoomDetailsEditEvent.Save) },
                    onDiscardClick = { state.eventSink(RoomDetailsEditEvent.OnBackPress) },
                    onDismiss = { state.eventSink(RoomDetailsEditEvent.CloseDialog) }
                )
            }
        },
        onSuccess = { onDone() },
        errorMessage = { stringResource(R.string.screen_room_details_edition_error) },
        onErrorDismiss = { state.eventSink(RoomDetailsEditEvent.CloseDialog) }
    )

    PermissionsView(
        state = state.cameraPermissionState,
    )
}

@PreviewsDayNight
@Composable
internal fun RoomDetailsEditViewPreview(@PreviewParameter(RoomDetailsEditStateProvider::class) state: RoomDetailsEditState) = ElementPreview {
    RoomDetailsEditView(
        state = state,
        onDone = {},
    )
}
