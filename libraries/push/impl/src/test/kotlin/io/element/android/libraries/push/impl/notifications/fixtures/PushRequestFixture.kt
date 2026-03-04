/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.fixtures

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.impl.db.PushRequest
import io.element.android.libraries.push.impl.push.PushRequestStatus

fun aPushRequest(
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    eventId: EventId = AN_EVENT_ID,
    providerInfo: String = "firebase",
    status: PushRequestStatus = PushRequestStatus.PENDING,
    retries: Int = 0,
) = PushRequest(
    pushDate = System.currentTimeMillis(),
    providerInfo = providerInfo,
    eventId = eventId.value,
    roomId = roomId.value,
    sessionId = sessionId.value,
    status = status.value,
    retries = retries.toLong(),
)
