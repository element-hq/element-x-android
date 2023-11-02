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

package io.element.android.features.roomlist.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListContextMenu(
    contextMenu: RoomListState.ContextMenu.Shown,
    eventSink: (RoomListEvents) -> Unit,
    onRoomSettingsClicked: (roomId: RoomId) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { eventSink(RoomListEvents.HideContextMenu) },
    ) {
        RoomListModalBottomSheetContent(
            contextMenu = contextMenu,
            onRoomSettingsClicked = {
                eventSink(RoomListEvents.HideContextMenu)
                onRoomSettingsClicked(it)
            },
            onLeaveRoomClicked = {
                eventSink(RoomListEvents.HideContextMenu)
                eventSink(RoomListEvents.LeaveRoom(contextMenu.roomId))
            }
        )
    }
}

@Composable
private fun RoomListModalBottomSheetContent(
    contextMenu: RoomListState.ContextMenu.Shown,
    onRoomSettingsClicked: (roomId: RoomId) -> Unit,
    onLeaveRoomClicked: (roomId: RoomId) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = contextMenu.roomName,
                    style = ElementTheme.typography.fontBodyLgMedium,
                )
            }
        )
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(id = CommonStrings.common_settings),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            modifier = Modifier.clickable { onRoomSettingsClicked(contextMenu.roomId) },
            leadingContent = {
                Icon(
                    resourceId = CommonDrawables.ic_compound_settings,
                    contentDescription = stringResource(id = CommonStrings.common_settings),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        )
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(id = CommonStrings.action_leave_room),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            modifier = Modifier.clickable { onLeaveRoomClicked(contextMenu.roomId) },
            leadingContent = {
                Icon(
                    resourceId = CommonDrawables.ic_compound_leave,
                    contentDescription = stringResource(id = CommonStrings.action_leave_room),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// TODO This component should be seen in [RoomListView] @Preview but it doesn't show up.
// see: https://issuetracker.google.com/issues/283843380
// Remove this preview when the issue is fixed.
@PreviewsDayNight
@Composable
internal fun RoomListModalBottomSheetContentPreview() = ElementPreview {
    RoomListModalBottomSheetContent(
        contextMenu = RoomListState.ContextMenu.Shown(
            roomId = RoomId(value = "!aRoom:aDomain"),
            roomName = "aRoom"
        ),
        onRoomSettingsClicked = {},
        onLeaveRoomClicked = {}
    )
}
