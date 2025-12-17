/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.api

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId

/**
 * Represent parsed data that the app has received from a Push content.
 *
 * @property eventId The Event Id.
 * @property roomId The Room Id.
 * @property unread Number of unread message.
 * @property clientSecret data used when the pusher was configured, to be able to determine the session.
 */
data class PushData(
    val eventId: EventId,
    val roomId: RoomId,
    val unread: Int?,
    val clientSecret: String,
)
