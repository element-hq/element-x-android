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

package io.element.android.features.messages.impl.messagecomposer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.R
import io.element.android.libraries.androidutils.ui.hideKeyboard
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AttachmentsBottomSheet(
    state: MessageComposerState,
    onSendLocationClicked: () -> Unit,
    onCreatePollClicked: () -> Unit,
    enableTextFormatting: Boolean,
    modifier: Modifier = Modifier,
) {
    val localView = LocalView.current
    var isVisible by rememberSaveable { mutableStateOf(state.showAttachmentSourcePicker) }

    BackHandler(enabled = isVisible) {
        isVisible = false
    }

    LaunchedEffect(state.showAttachmentSourcePicker) {
        if (state.showAttachmentSourcePicker) {
            // We need to use this instead of `LocalFocusManager.clearFocus()` to hide the keyboard when focus is on an Android View
            localView.hideKeyboard()
            isVisible = true
        } else {
            isVisible = false
        }
    }
    // Send 'DismissAttachmentMenu' event when the bottomsheet was just hidden
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            state.eventSink(MessageComposerEvents.DismissAttachmentMenu)
        }
    }

    if (isVisible) {
        ModalBottomSheet(
            modifier = modifier,
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            onDismissRequest = { isVisible = false }
        ) {
            AttachmentSourcePickerMenu(
                state = state,
                enableTextFormatting = enableTextFormatting,
                onSendLocationClicked = onSendLocationClicked,
                onCreatePollClicked = onCreatePollClicked,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AttachmentSourcePickerMenu(
    state: MessageComposerState,
    onSendLocationClicked: () -> Unit,
    onCreatePollClicked: () -> Unit,
    enableTextFormatting: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.padding(bottom = 32.dp)
//        .navigationBarsPadding() - FIXME after https://issuetracker.google.com/issues/275849044
    ) {
        ListItem(
            modifier = Modifier.clickable { state.eventSink(MessageComposerEvents.PickAttachmentSource.FromGallery) },
            icon = { Icon(CommonDrawables.ic_september_photo_video_library, null) },
            text = { Text(stringResource(R.string.screen_room_attachment_source_gallery)) },
        )
        ListItem(
            modifier = Modifier.clickable { state.eventSink(MessageComposerEvents.PickAttachmentSource.FromFiles) },
            icon = { Icon(CommonDrawables.ic_september_attachment, null) },
            text = { Text(stringResource(R.string.screen_room_attachment_source_files)) },
        )
        ListItem(
            modifier = Modifier.clickable { state.eventSink(MessageComposerEvents.PickAttachmentSource.PhotoFromCamera) },
            icon = { Icon(CommonDrawables.ic_september_take_photo_camera, null) },
            text = { Text(stringResource(R.string.screen_room_attachment_source_camera_photo)) },
        )
        ListItem(
            modifier = Modifier.clickable { state.eventSink(MessageComposerEvents.PickAttachmentSource.VideoFromCamera) },
            icon = { Icon(CommonDrawables.ic_september_video_call, null) },
            text = { Text(stringResource(R.string.screen_room_attachment_source_camera_video)) },
        )
        if (state.canShareLocation) {
            ListItem(
                modifier = Modifier.clickable {
                    state.eventSink(MessageComposerEvents.PickAttachmentSource.Location)
                    onSendLocationClicked()
                },
                icon = { Icon(CommonDrawables.ic_september_location, null) },
                text = { Text(stringResource(R.string.screen_room_attachment_source_location)) },
            )
        }
        if (state.canCreatePoll) {
            ListItem(
                modifier = Modifier.clickable {
                    state.eventSink(MessageComposerEvents.PickAttachmentSource.Poll)
                    onCreatePollClicked()
                },
                icon = { Icon(CommonDrawables.ic_compound_polls, null) },
                text = { Text(stringResource(R.string.screen_room_attachment_source_poll)) },
            )
        }
        if (enableTextFormatting) {
            ListItem(
                modifier = Modifier.clickable { state.eventSink(MessageComposerEvents.ToggleTextFormatting(enabled = true)) },
                icon = { Icon(CommonDrawables.ic_september_text_formatting, null) },
                text = { Text(stringResource(R.string.screen_room_attachment_text_formatting)) },
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun AttachmentSourcePickerMenuPreview() = ElementPreview {
    AttachmentSourcePickerMenu(
        state = aMessageComposerState(
            canShareLocation = true,
        ),
        onSendLocationClicked = {},
        onCreatePollClicked = {},
        enableTextFormatting = true,
    )
}
