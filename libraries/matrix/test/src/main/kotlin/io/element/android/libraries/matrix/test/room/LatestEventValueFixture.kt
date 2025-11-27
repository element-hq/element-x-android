/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.roomlist.LatestEventValue
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.aProfileDetails

fun aRemoteLatestEvent(
    content: EventContent = aMessageContent(),
    timestamp: Long = 0L,
    isOwn: Boolean = false,
    senderId: UserId = A_USER_ID,
    senderProfile: ProfileDetails = aProfileDetails(),
): LatestEventValue.Remote {
    return LatestEventValue.Remote(
        timestamp = timestamp,
        content = content,
        senderId = senderId,
        senderProfile = senderProfile,
        isOwn = isOwn,
    )
}

fun aLocalLatestEvent(
    content: EventContent = aMessageContent(),
    timestamp: Long = 0L,
    isSending: Boolean = false,
    senderId: UserId = A_USER_ID,
    senderProfile: ProfileDetails = aProfileDetails(),
): LatestEventValue.Local {
    return LatestEventValue.Local(
        timestamp = timestamp,
        content = content,
        senderId = senderId,
        senderProfile = senderProfile,
        isSending = isSending,
    )
}
