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

    TimelineRoom(32.dp),
    TimelineSender(32.dp),
    TimelineReadReceipt(16.dp),

    ReadReceiptList(32.dp),

    MessageActionSender(32.dp),

    RoomInviteItem(52.dp),
    InviteSender(16.dp),

    EditRoomDetails(70.dp),
    RoomListManageUser(70.dp),

    NotificationsOptIn(32.dp),

    CustomRoomNotificationSetting(36.dp),

    RoomDirectoryItem(36.dp),
}
