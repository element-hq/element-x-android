/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.timeline.item.event

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.test.A_USER_ID

fun aRoomMembershipContent(
    userId: UserId = A_USER_ID,
    userDisplayName: String? = null,
    change: MembershipChange? = null,
    reason: String? = null,
) = RoomMembershipContent(
    userId = userId,
    userDisplayName = userDisplayName,
    change = change,
    reason = reason,
)
