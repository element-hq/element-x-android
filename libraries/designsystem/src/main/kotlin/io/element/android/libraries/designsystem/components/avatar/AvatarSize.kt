/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AvatarSize(val dp: Dp) {
    CurrentUserTopBar(32.dp),

    IncomingCall(140.dp),
    RoomDetailsHeader(96.dp),
    RoomListItem(52.dp),

    SpaceListItem(52.dp),

    RoomSelectRoomListItem(36.dp),

    UserPreference(56.dp),

    UserHeader(96.dp),
    UserListItem(36.dp),

    SelectedUser(52.dp),
    SelectedRoom(56.dp),

    DmCluster(75.dp),

    TimelineRoom(32.dp),
    TimelineSender(32.dp),
    TimelineReadReceipt(16.dp),
    TimelineThreadLatestEventSender(24.dp),

    ComposerAlert(32.dp),

    ReadReceiptList(32.dp),

    MessageActionSender(32.dp),

    RoomInviteItem(52.dp),
    InviteSender(16.dp),

    EditRoomDetails(70.dp),
    RoomListManageUser(96.dp),

    NotificationsOptIn(32.dp),

    CustomRoomNotificationSetting(36.dp),

    RoomDirectoryItem(36.dp),

    EditProfileDetails(96.dp),

    Suggestion(32.dp),

    KnockRequestItem(52.dp),
    KnockRequestBanner(32.dp),

    MediaSender(32.dp),

    DmCreationConfirmation(64.dp),

    UserVerification(52.dp),

    OrganizationHeader(64.dp),
    SpaceHeader(64.dp),
    RoomPreviewHeader(64.dp),
    RoomPreviewInviter(56.dp),
    SpaceMember(24.dp),
    LeaveSpaceRoom(32.dp),

    AccountItem(32.dp),
}
