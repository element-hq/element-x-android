/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AvatarSize(val dp: Dp) {
    CurrentUserTopBar(32.dp),

    IncomingCall(140.dp),
    RoomHeader(96.dp),
    RoomListItem(52.dp),

    RoomSelectRoomListItem(36.dp),

    UserPreference(56.dp),

    UserHeader(96.dp),
    UserListItem(36.dp),

    SelectedUser(56.dp),
    SelectedRoom(56.dp),

    DmCluster(75.dp),

    TimelineRoom(32.dp),
    TimelineSender(32.dp),
    TimelineReadReceipt(16.dp),

    ComposerAlert(32.dp),

    ReadReceiptList(32.dp),

    MessageActionSender(32.dp),

    RoomInviteItem(52.dp),
    InviteSender(16.dp),

    EditRoomDetails(70.dp),
    RoomListManageUser(70.dp),

    NotificationsOptIn(32.dp),

    CustomRoomNotificationSetting(36.dp),

    RoomDirectoryItem(36.dp),

    EditProfileDetails(96.dp),

    Suggestion(32.dp),
}
