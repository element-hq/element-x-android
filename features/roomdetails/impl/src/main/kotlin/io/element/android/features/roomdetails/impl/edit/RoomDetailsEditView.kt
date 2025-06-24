/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.roomdetails.impl.edit

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
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncActionViewDefaults
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.modifiers.clearFocusOnTap
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
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
    onBackClick: () -> Unit,
    onRoomEditSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val isAvatarActionsSheetVisible = remember { mutableStateOf(false) }

    fun onAvatarClick() {
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
                navigationIcon = { BackButton(onClick = onBackClick) },
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
                avatarType = AvatarType.Room(),
                onAvatarClick = ::onAvatarClick,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(60.dp))

            TextField(
                label = stringResource(id = R.string.screen_room_details_room_name_label),
                value = state.roomRawName,
                placeholder = stringResource(CommonStrings.common_room_name_placeholder),
                singleLine = true,
                readOnly = !state.canChangeName,
                onValueChange = { state.eventSink(RoomDetailsEditEvents.UpdateRoomName(it)) },
            )

            Spacer(modifier = Modifier.height(28.dp))

            TextField(
                label = stringResource(CommonStrings.common_topic),
                value = state.roomTopic,
                placeholder = stringResource(CommonStrings.common_topic_placeholder),
                maxLines = 10,
                readOnly = !state.canChangeTopic,
                onValueChange = { state.eventSink(RoomDetailsEditEvents.UpdateRoomTopic(it)) },
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
        onSelectAction = { state.eventSink(RoomDetailsEditEvents.HandleAvatarAction(it)) }
    )

    AsyncActionView(
        async = state.saveAction,
        progressDialog = {
            AsyncActionViewDefaults.ProgressDialog(
                progressText = stringResource(R.string.screen_room_details_updating_room),
            )
        },
        onSuccess = { onRoomEditSuccess() },
        errorMessage = { stringResource(R.string.screen_room_details_edition_error) },
        onErrorDismiss = { state.eventSink(RoomDetailsEditEvents.CancelSaveChanges) }
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
        onBackClick = {},
        onRoomEditSuccess = {},
    )
}
