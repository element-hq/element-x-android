/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.test.A_USER_ID
import org.matrix.rustcomponents.sdk.MembershipChange
import org.matrix.rustcomponents.sdk.TimelineItemContent

internal fun aRustTimelineItemContentProfileChange(
    displayName: String? = "new-name",
    prevDisplayName: String? = "old-name",
    avatarUrl: String? = "mxc://example.org/new",
    prevAvatarUrl: String? = "mxc://example.org/old",
) = TimelineItemContent.ProfileChange(
    displayName = displayName,
    prevDisplayName = prevDisplayName,
    avatarUrl = avatarUrl,
    prevAvatarUrl = prevAvatarUrl,
)

internal fun aRustTimelineItemContentRoomMembership(
    userId: String = A_USER_ID.value,
    userDisplayName: String? = "name",
    change: MembershipChange? = MembershipChange.JOINED,
    reason: String? = null,
) = TimelineItemContent.RoomMembership(
    userId = userId,
    userDisplayName = userDisplayName,
    change = change,
    reason = reason,
)
