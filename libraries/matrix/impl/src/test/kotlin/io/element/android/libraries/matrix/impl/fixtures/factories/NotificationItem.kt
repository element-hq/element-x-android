/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiTimelineEvent
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_USER_NAME
import org.matrix.rustcomponents.sdk.JoinRule
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
    threadId: ThreadId? = null,
) = NotificationItem(
    event = event,
    senderInfo = senderInfo,
    roomInfo = roomInfo,
    isNoisy = isNoisy,
    hasMention = hasMention,
    threadId = threadId?.value,
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
    joinRule: JoinRule? = null,
    isPublic: Boolean = true,
) = NotificationRoomInfo(
    displayName = displayName,
    avatarUrl = avatarUrl,
    canonicalAlias = canonicalAlias,
    joinedMembersCount = joinedMembersCount,
    isEncrypted = isEncrypted,
    isDirect = isDirect,
    joinRule = joinRule,
    isPublic = isPublic,
)

fun aRustNotificationEventTimeline(
    event: TimelineEvent = FakeFfiTimelineEvent(),
) = NotificationEvent.Timeline(
    event = event,
)
