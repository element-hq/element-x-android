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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.VectorIcons
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.R as StringR

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
                    fontWeight = FontWeight.Bold,
                )
            }
        )
        ListItem(
            headlineContent = {
                Text(text = stringResource(id = StringR.string.common_settings))
            },
            modifier = Modifier.clickable { onRoomSettingsClicked(contextMenu.roomId) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(id = StringR.string.common_settings),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        )
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(id = StringR.string.action_leave_room),
                    color = ElementTheme.colors.textActionCritical,
                )
            },
            modifier = Modifier.clickable { onLeaveRoomClicked(contextMenu.roomId) },
            leadingContent = {
                Icon(
                    resourceId = VectorIcons.DoorOpen,
                    contentDescription = stringResource(id = StringR.string.action_leave_room),
                    modifier = Modifier.size(20.dp),
                    tint = ElementTheme.colors.textActionCritical,
                )
            }
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// TODO This component should be seen in [RoomListView] @Preview but it doesn't show up.
// see: https://issuetracker.google.com/issues/283843380
// Remove this preview when the issue is fixed.
@Preview
@Composable
internal fun RoomListModalBottomSheetContentLightPreview() =
    ElementPreviewLight { ContentToPreview() }

// TODO This component should be seen in [RoomListView] @Preview but it doesn't show up.
// see: https://issuetracker.google.com/issues/283843380
// Remove this preview when the issue is fixed.
@Preview
@Composable
internal fun RoomListModalBottomSheetContentDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    RoomListModalBottomSheetContent(
        contextMenu = RoomListState.ContextMenu.Shown(
            roomId = RoomId(value = "!aRoom:aDomain"),
            roomName = "aRoom"
        ),
        onRoomSettingsClicked = {},
        onLeaveRoomClicked = {}
    )
}
