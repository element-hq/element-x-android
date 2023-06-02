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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
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
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AttachmentsBottomSheet(
    state: MessageComposerState,
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
            onDismissRequest = { isVisible = false }
        ) {
            AttachmentSourcePickerMenu(eventSink = state.eventSink)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun AttachmentSourcePickerMenu(
    eventSink: (MessageComposerEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.padding(bottom = 32.dp)
//        .navigationBarsPadding() - FIXME after https://issuetracker.google.com/issues/275849044
    ) {
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
