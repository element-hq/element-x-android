/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.roomlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.home.impl.R
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
    canReportRoom: Boolean,
    eventSink: (RoomListEvents.ContextMenuEvents) -> Unit,
    onRoomSettingsClick: (roomId: RoomId) -> Unit,
    onReportRoomClick: (roomId: RoomId) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { eventSink(RoomListEvents.HideContextMenu) },
    ) {
        RoomListModalBottomSheetContent(
            contextMenu = contextMenu,
            canReportRoom = canReportRoom,
            onRoomMarkReadClick = {
                eventSink(RoomListEvents.HideContextMenu)
                eventSink(RoomListEvents.MarkAsRead(contextMenu.roomId))
            },
            onRoomMarkUnreadClick = {
                eventSink(RoomListEvents.HideContextMenu)
                eventSink(RoomListEvents.MarkAsUnread(contextMenu.roomId))
            },
            onRoomSettingsClick = {
                eventSink(RoomListEvents.HideContextMenu)
                onRoomSettingsClick(contextMenu.roomId)
            },
            onLeaveRoomClick = {
                eventSink(RoomListEvents.HideContextMenu)
                eventSink(RoomListEvents.LeaveRoom(contextMenu.roomId, needsConfirmation = true))
            },
            onFavoriteChange = { isFavorite ->
                eventSink(RoomListEvents.SetRoomIsFavorite(contextMenu.roomId, isFavorite))
            },
            onClearCacheRoomClick = {
                eventSink(RoomListEvents.HideContextMenu)
                eventSink(RoomListEvents.ClearCacheOfRoom(contextMenu.roomId))
            },
            onReportRoomClick = {
                eventSink(RoomListEvents.HideContextMenu)
                onReportRoomClick(contextMenu.roomId)
            },
        )
    }
}

@Composable
private fun RoomListModalBottomSheetContent(
    contextMenu: RoomListState.ContextMenu.Shown,
    canReportRoom: Boolean,
    onRoomSettingsClick: () -> Unit,
    onLeaveRoomClick: () -> Unit,
    onFavoriteChange: (isFavorite: Boolean) -> Unit,
    onRoomMarkReadClick: () -> Unit,
    onRoomMarkUnreadClick: () -> Unit,
    onClearCacheRoomClick: () -> Unit,
    onReportRoomClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = contextMenu.roomName ?: stringResource(id = CommonStrings.common_no_room_name),
                    style = ElementTheme.typography.fontBodyLgMedium,
                    fontStyle = FontStyle.Italic.takeIf { contextMenu.roomName == null }
                )
            }
        )
        if (contextMenu.hasNewContent) {
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(id = R.string.screen_roomlist_mark_as_read),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                onClick = onRoomMarkReadClick,
                leadingContent = ListItemContent.Icon(
                    iconSource = IconSource.Vector(CompoundIcons.MarkAsRead())
                ),
                style = ListItemStyle.Primary,
            )
        } else {
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(id = R.string.screen_roomlist_mark_as_unread),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                onClick = onRoomMarkUnreadClick,
                leadingContent = ListItemContent.Icon(
                    iconSource = IconSource.Vector(CompoundIcons.MarkAsUnread())
                ),
                style = ListItemStyle.Primary,
            )
        }
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(id = CommonStrings.common_favourite),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            leadingContent = ListItemContent.Icon(
                iconSource = IconSource.Vector(
                    CompoundIcons.Favourite(),
                )
            ),
            trailingContent = ListItemContent.Switch(
                checked = contextMenu.isFavorite,
            ),
            onClick = {
                onFavoriteChange(!contextMenu.isFavorite)
            },
            style = ListItemStyle.Primary,
        )
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(id = CommonStrings.common_settings),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            modifier = Modifier.clickable { onRoomSettingsClick() },
            leadingContent = ListItemContent.Icon(
                iconSource = IconSource.Vector(
                    CompoundIcons.Settings(),
                )
            ),
            style = ListItemStyle.Primary,
        )
        if (canReportRoom) {
            ListItem(
                headlineContent = {
                    Text(text = stringResource(CommonStrings.action_report_room))
                },
                modifier = Modifier.clickable { onReportRoomClick() },
                leadingContent = ListItemContent.Icon(
                    iconSource = IconSource.Vector(
                        CompoundIcons.ChatProblem(),
                    )
                ),
                style = ListItemStyle.Destructive,
            )
        }
        ListItem(
            headlineContent = {
                Text(text = stringResource(CommonStrings.action_leave_room))
            },
            modifier = Modifier.clickable { onLeaveRoomClick() },
            leadingContent = ListItemContent.Icon(
                iconSource = IconSource.Vector(
                    CompoundIcons.Leave(),
                )
            ),
            style = ListItemStyle.Destructive,
        )
        if (contextMenu.displayClearRoomCacheAction) {
            ListItem(
                headlineContent = {
                    Text(text = "Clear cache for this room")
                },
                modifier = Modifier.clickable { onClearCacheRoomClick() },
                leadingContent = ListItemContent.Icon(
                    iconSource = IconSource.Vector(CompoundIcons.Delete())
                ),
                style = ListItemStyle.Primary,
            )
        }
    }
}

// TODO This component should be seen in [RoomListView] @Preview but it doesn't show up.
// see: https://issuetracker.google.com/issues/283843380
// Remove this preview when the issue is fixed.
@PreviewsDayNight
@Composable
internal fun RoomListModalBottomSheetContentPreview(
    @PreviewParameter(RoomListStateContextMenuShownProvider::class) contextMenu: RoomListState.ContextMenu.Shown
) = ElementPreview {
    RoomListModalBottomSheetContent(
        contextMenu = contextMenu,
        canReportRoom = true,
        onRoomMarkReadClick = {},
        onRoomMarkUnreadClick = {},
        onRoomSettingsClick = {},
        onLeaveRoomClick = {},
        onFavoriteChange = {},
        onClearCacheRoomClick = {},
        onReportRoomClick = {},
    )
}
