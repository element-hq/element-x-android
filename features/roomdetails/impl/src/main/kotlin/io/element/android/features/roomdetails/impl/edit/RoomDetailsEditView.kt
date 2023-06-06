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

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)

package io.element.android.features.roomdetails.impl.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.components.LabelledTextField
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.LocalColors
import io.element.android.libraries.designsystem.theme.components.CenterAlignedTopAppBar
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.matrix.ui.components.AvatarActionBottomSheet
import io.element.android.libraries.matrix.ui.components.UnsavedAvatar
import kotlinx.coroutines.launch
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailsEditView(
    state: RoomDetailsEditState,
    onBackPressed: () -> Unit,
    onRoomEdited: () -> Unit,
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.screen_room_details_edit_room_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = { BackButton(onClick = onBackPressed) },
                actions = {
                    TextButton(
                        enabled = state.saveButtonEnabled,
                        onClick = {
                            focusManager.clearFocus()
                            state.eventSink(RoomDetailsEditEvents.Save)
                        },
                    ) {
                        Text(
                            text = stringResource(StringR.string.action_save),
                            fontSize = 16.sp,
                        )
                    }
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
            EditableAvatarView(state, ::onAvatarClicked)
            Spacer(modifier = Modifier.height(60.dp))

            if (state.canChangeName) {
                LabelledTextField(
                    label = stringResource(id = R.string.screen_room_details_room_name_label),
                    value = state.roomName,
                    placeholder = stringResource(StringR.string.common_room_name_placeholder),
                    singleLine = true,
                    onValueChange = { state.eventSink(RoomDetailsEditEvents.UpdateRoomName(it)) },
                )
            } else {
                LabelledReadOnlyField(
                    title = stringResource(R.string.screen_room_details_room_name_label),
                    value = state.roomName
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (state.canChangeTopic) {
                LabelledTextField(
                    label = stringResource(StringR.string.common_topic),
                    value = state.roomTopic,
                    placeholder = stringResource(StringR.string.common_topic_placeholder),
                    maxLines = 10,
                    onValueChange = { state.eventSink(RoomDetailsEditEvents.UpdateRoomTopic(it)) },
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
        modalBottomSheetState = itemActionsBottomSheetState,
        onActionSelected = { state.eventSink(RoomDetailsEditEvents.HandleAvatarAction(it)) }
    )

    when (state.saveAction) {
        is Async.Loading -> {
            ProgressDialog(text = stringResource(R.string.screen_room_details_updating_room))
        }

        is Async.Failure -> {
            ErrorDialog(
                content = stringResource(R.string.screen_room_details_edition_error),
                onDismiss = { state.eventSink(RoomDetailsEditEvents.CancelSaveChanges) },
            )
        }

        is Async.Success -> {
            LaunchedEffect(state.saveAction) {
                onRoomEdited()
            }
        }

        else -> Unit
    }
}

@Composable
private fun EditableAvatarView(
    state: RoomDetailsEditState,
    onAvatarClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clickable(onClick = onAvatarClicked, enabled = state.canChangeAvatar)
        ) {
            // TODO this might be able to be simplified into a single component once send/receive media is done
            when (state.roomAvatarUrl?.scheme) {
                null, "mxc" -> {
                    Avatar(
                        avatarData = AvatarData(state.roomId, state.roomName, state.roomAvatarUrl?.toString(), size = AvatarSize.HUGE),
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    UnsavedAvatar(
                        avatarUri = state.roomAvatarUrl,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            if (state.canChangeAvatar) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(LocalColors.current.gray1400)
                        .size(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Outlined.AddAPhoto,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun LabelledReadOnlyField(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleSmall,
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

private fun Modifier.clearFocusOnTap(focusManager: FocusManager): Modifier =
    pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
        })
    }

@Preview
@Composable
fun RoomDetailsEditViewLightPreview(@PreviewParameter(RoomDetailsEditStateProvider::class) state: RoomDetailsEditState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun RoomDetailsEditViewDarkPreview(@PreviewParameter(RoomDetailsEditStateProvider::class) state: RoomDetailsEditState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: RoomDetailsEditState) {
    RoomDetailsEditView(
        state = state,
        onBackPressed = {},
        onRoomEdited = {},
    )
}
