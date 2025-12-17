/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.pushproviders.api.PushData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * In this case, the format is:
 * <pre>
 * {
 *     "notification":{
 *         "event_id":"$anEventId",
 *         "room_id":"!aRoomId",
 *         "counts":{
 *             "unread":1
 *         },
 *         "prio":"high"
 *     }
 * }
 * </pre>
 * .
 */
@Serializable
data class PushDataUnifiedPush(
    val notification: PushDataUnifiedPushNotification? = null
)

@Serializable
data class PushDataUnifiedPushNotification(
    @SerialName("event_id") val eventId: String? = null,
    @SerialName("room_id") val roomId: String? = null,
    @SerialName("counts") val counts: PushDataUnifiedPushCounts? = null,
    @SerialName("prio") val prio: String? = null,
)

@Serializable
data class PushDataUnifiedPushCounts(
    @SerialName("unread") val unread: Int? = null
)

fun PushDataUnifiedPush.toPushData(clientSecret: String): PushData? {
    val safeEventId = notification?.eventId?.let(::EventId) ?: return null
    val safeRoomId = notification.roomId?.let(::RoomId) ?: return null
    return PushData(
        eventId = safeEventId,
        roomId = safeRoomId,
        unread = notification.counts?.unread,
        clientSecret = clientSecret
    )
}
