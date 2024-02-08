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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListContextMenu(
    contextMenu: RoomListState.ContextMenu.Shown,
    eventSink: (RoomListEvents.RoomListBottomSheetEvents) -> Unit,
    onRoomSettingsClicked: (roomId: RoomId) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { eventSink(RoomListEvents.HideContextMenu) },
    ) {
        RoomListModalBottomSheetContent(
            contextMenu = contextMenu,
            onRoomMarkReadClicked = {
                eventSink(RoomListEvents.HideContextMenu)
                eventSink(RoomListEvents.MarkAsRead(contextMenu.roomId))
            },
            onRoomMarkUnreadClicked = {
                eventSink(RoomListEvents.HideContextMenu)
                eventSink(RoomListEvents.MarkAsUnread(contextMenu.roomId))
            },
            onRoomSettingsClicked = {
                eventSink(RoomListEvents.HideContextMenu)
                onRoomSettingsClicked(contextMenu.roomId)
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
    onRoomMarkReadClicked: () -> Unit,
    onRoomMarkUnreadClicked: () -> Unit,
    onRoomSettingsClicked: () -> Unit,
    onLeaveRoomClicked: () -> Unit,
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
        if (contextMenu.markAsUnreadFeatureFlagEnabled) {
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(
                            id = if (contextMenu.hasNewContent) {
                                R.string.screen_roomlist_mark_as_read
                            } else {
                                R.string.screen_roomlist_mark_as_unread
                            }
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                modifier = Modifier.clickable {
                    if (contextMenu.hasNewContent) {
                        onRoomMarkReadClicked()
                    } else {
                        onRoomMarkUnreadClicked()
                    }
                },
                /* TODO Design
                leadingContent = ListItemContent.Icon(
                    iconSource = IconSource.Vector(
                        CompoundIcons.Settings,
                        contentDescription = stringResource(id = CommonStrings.common_settings)
                    )
                ),
                 */
                style = ListItemStyle.Primary,
            )
        }
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(id = CommonStrings.common_settings),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            modifier = Modifier.clickable { onRoomSettingsClicked() },
            leadingContent = ListItemContent.Icon(
                iconSource = IconSource.Vector(
                    CompoundIcons.Settings,
                    contentDescription = stringResource(id = CommonStrings.common_settings)
                )
            ),
            style = ListItemStyle.Primary,
        )
        ListItem(
            headlineContent = {
                val leaveText = stringResource(
                    id = if (contextMenu.isDm) {
                        CommonStrings.action_leave_conversation
                    } else {
                        CommonStrings.action_leave_room
                    }
                )
                Text(text = leaveText)
            },
            modifier = Modifier.clickable { onLeaveRoomClicked() },
            leadingContent = ListItemContent.Icon(
                iconSource = IconSource.Vector(
                    CompoundIcons.Leave,
                    contentDescription = stringResource(id = CommonStrings.action_leave_room)
                )
            ),
            style = ListItemStyle.Destructive,
        )
    }
}

// TODO This component should be seen in [RoomListView] @Preview but it doesn't show up.
// see: https://issuetracker.google.com/issues/283843380
// Remove this preview when the issue is fixed.
@PreviewsDayNight
@Composable
internal fun RoomListModalBottomSheetContentPreview() = ElementPreview {
    RoomListModalBottomSheetContent(
        contextMenu = aContextMenuShown(hasNewContent = true),
        onRoomMarkReadClicked = {},
        onRoomMarkUnreadClicked = {},
        onRoomSettingsClicked = {},
        onLeaveRoomClicked = {}
    )
}

@PreviewsDayNight
@Composable
internal fun RoomListModalBottomSheetContentForDmPreview() = ElementPreview {
    RoomListModalBottomSheetContent(
        contextMenu = aContextMenuShown(isDm = true),
        onRoomMarkReadClicked = {},
        onRoomMarkUnreadClicked = {},
        onRoomSettingsClicked = {},
        onLeaveRoomClicked = {}
    )
}
