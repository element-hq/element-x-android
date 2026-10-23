/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

data class RoomStateEvent(
    val eventType: String,
    val stateKey: String,
    val sender: UserId,
    val eventId: EventId?,
    val timestamp: Long?,
    val contentJson: String,
)
