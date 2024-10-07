/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustTimelineEvent
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_USER_NAME
import org.matrix.rustcomponents.sdk.NotificationEvent
import org.matrix.rustcomponents.sdk.NotificationItem
import org.matrix.rustcomponents.sdk.NotificationRoomInfo
import org.matrix.rustcomponents.sdk.NotificationSenderInfo
import org.matrix.rustcomponents.sdk.TimelineEvent

fun aRustNotificationItem(
    event: NotificationEvent = aRustNotificationEventTimeline(),
    senderInfo: NotificationSenderInfo = aRustNotificationSenderInfo(),
    roomInfo: NotificationRoomInfo = aRustNotificationRoomInfo(),
    isNoisy: Boolean? = false,
    hasMention: Boolean? = false,
) = NotificationItem(
    event = event,
    senderInfo = senderInfo,
    roomInfo = roomInfo,
    isNoisy = isNoisy,
    hasMention = hasMention,
)

fun aRustNotificationSenderInfo(
    displayName: String? = A_USER_NAME,
    avatarUrl: String? = null,
    isNameAmbiguous: Boolean = false,
) = NotificationSenderInfo(
    displayName = displayName,
    avatarUrl = avatarUrl,
    isNameAmbiguous = isNameAmbiguous,
)

fun aRustNotificationRoomInfo(
    displayName: String = A_ROOM_NAME,
    avatarUrl: String? = null,
    canonicalAlias: String? = null,
    joinedMembersCount: ULong = 2u,
    isEncrypted: Boolean? = true,
    isDirect: Boolean = false,
) = NotificationRoomInfo(
    displayName = displayName,
    avatarUrl = avatarUrl,
    canonicalAlias = canonicalAlias,
    joinedMembersCount = joinedMembersCount,
    isEncrypted = isEncrypted,
    isDirect = isDirect,
)

fun aRustNotificationEventTimeline(
    event: TimelineEvent = FakeRustTimelineEvent(),
) = NotificationEvent.Timeline(
    event = event,
)
