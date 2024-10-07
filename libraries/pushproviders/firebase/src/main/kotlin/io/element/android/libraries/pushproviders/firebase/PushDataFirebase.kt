/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.pushproviders.api.PushData

/**
 * In this case, the format is:
 * <pre>
 * {
 *     "event_id":"$anEventId",
 *     "room_id":"!aRoomId",
 *     "unread":"1",
 *     "prio":"high",
 *     "cs":"<client_secret>"
 * }
 * </pre>
 * .
 */
data class PushDataFirebase(
    val eventId: String?,
    val roomId: String?,
    val unread: Int?,
    val clientSecret: String?
)

fun PushDataFirebase.toPushData(): PushData? {
    val safeEventId = eventId?.let(::EventId) ?: return null
    val safeRoomId = roomId?.let(::RoomId) ?: return null
    return PushData(
        eventId = safeEventId,
        roomId = safeRoomId,
        unread = unread,
        clientSecret = clientSecret,
    )
}
