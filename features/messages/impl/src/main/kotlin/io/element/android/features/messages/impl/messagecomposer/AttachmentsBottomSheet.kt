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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.R
import io.element.android.libraries.androidutils.ui.hideKeyboard
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
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
        isVisible = if (state.showAttachmentSourcePicker) {
            // We need to use this instead of `LocalFocusManager.clearFocus()` to hide the keyboard when focus is on an Android View
            localView.hideKeyboard()
            true
        } else {
            false
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

@Composable
private fun AttachmentSourcePickerMenu(
    state: MessageComposerState,
    onSendLocationClicked: () -> Unit,
    onCreatePollClicked: () -> Unit,
    enableTextFormatting: Boolean,
) {
    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
    ) {
        ListItem(
            modifier = Modifier.clickable { state.eventSink(MessageComposerEvents.PickAttachmentSource.FromGallery) },
            leadingContent = ListItemContent.Icon(IconSource.Resource(CommonDrawables.ic_image)),
            headlineContent = { Text(stringResource(R.string.screen_room_attachment_source_gallery)) },
            style = ListItemStyle.Primary,
        )
        ListItem(
            modifier = Modifier.clickable { state.eventSink(MessageComposerEvents.PickAttachmentSource.FromFiles) },
            leadingContent = ListItemContent.Icon(IconSource.Resource(CommonDrawables.ic_attachment)),
            headlineContent = { Text(stringResource(R.string.screen_room_attachment_source_files)) },
            style = ListItemStyle.Primary,
        )
        ListItem(
            modifier = Modifier.clickable { state.eventSink(MessageComposerEvents.PickAttachmentSource.PhotoFromCamera) },
            leadingContent = ListItemContent.Icon(IconSource.Resource(CommonDrawables.ic_take_photo_camera)),
            headlineContent = { Text(stringResource(R.string.screen_room_attachment_source_camera_photo)) },
            style = ListItemStyle.Primary,
        )
        ListItem(
            modifier = Modifier.clickable { state.eventSink(MessageComposerEvents.PickAttachmentSource.VideoFromCamera) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.VideoCall())),
            headlineContent = { Text(stringResource(R.string.screen_room_attachment_source_camera_video)) },
            style = ListItemStyle.Primary,
        )
        if (state.canShareLocation) {
            ListItem(
                modifier = Modifier.clickable {
                    state.eventSink(MessageComposerEvents.PickAttachmentSource.Location)
                    onSendLocationClicked()
                },
                leadingContent = ListItemContent.Icon(IconSource.Resource(CommonDrawables.ic_location_pin)),
                headlineContent = { Text(stringResource(R.string.screen_room_attachment_source_location)) },
                style = ListItemStyle.Primary,
            )
        }
        if (state.canCreatePoll) {
            ListItem(
                modifier = Modifier.clickable {
                    state.eventSink(MessageComposerEvents.PickAttachmentSource.Poll)
                    onCreatePollClicked()
                },
                leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Polls())),
                headlineContent = { Text(stringResource(R.string.screen_room_attachment_source_poll)) },
                style = ListItemStyle.Primary,
            )
        }
        if (enableTextFormatting) {
            ListItem(
                modifier = Modifier.clickable { state.eventSink(MessageComposerEvents.ToggleTextFormatting(enabled = true)) },
                leadingContent = ListItemContent.Icon(IconSource.Resource(CommonDrawables.ic_text_formatting, null)),
                headlineContent = { Text(stringResource(R.string.screen_room_attachment_text_formatting)) },
                style = ListItemStyle.Primary,
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
