/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.history

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Data class representing a push history item.
 * @property pushDate Date (timestamp).
 * @property formattedDate Formatted date.
 * @property providerInfo Push provider name / info
 * @property eventId EventId from the push, can be null if the received data are not correct.
 * @property roomId RoomId from the push, can be null if the received data are not correct.
 * @property sessionId The session Id, can be null if the session cannot be retrieved
 * @property hasBeenResolved Result of resolving the event
 * @property comment Comment. Can contains an error message if the event could not be resolved, or other any information.
 */
data class PushHistoryItem(
    val pushDate: Long,
    val formattedDate: String,
    val providerInfo: String,
    val eventId: EventId?,
    val roomId: RoomId?,
    val sessionId: SessionId?,
    val hasBeenResolved: Boolean,
    val comment: String?,
)
