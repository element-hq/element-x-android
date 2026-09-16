/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import org.matrix.rustcomponents.sdk.MessageSearchResult
import org.matrix.rustcomponents.sdk.ProfileDetails
import org.matrix.rustcomponents.sdk.SearchServiceResult

internal fun aRustSearchServiceResult(
    eventId: String,
    roomId: String = A_ROOM_ID.value,
    sender: String = A_USER_ID.value,
    body: String = "Hello",
    timestamp: ULong = 0uL,
) = SearchServiceResult.Message(
    roomId = roomId,
    result = MessageSearchResult(
        eventId = eventId,
        sender = sender,
        senderProfile = ProfileDetails.Unavailable,
        content = aRustTimelineItemContentMsgLike(body = body),
        timestamp = timestamp,
    ),
)
