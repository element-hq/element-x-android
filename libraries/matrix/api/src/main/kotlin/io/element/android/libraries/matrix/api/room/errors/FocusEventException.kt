/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.errors

import io.element.android.libraries.matrix.api.core.EventId

sealed class FocusEventException : Exception() {
    data class InvalidEventId(
        val eventId: String,
        val err: String
    ) : FocusEventException()

    data class EventNotFound(
        val eventId: EventId
    ) : FocusEventException()

    data class Other(
        val msg: String
    ) : FocusEventException()
}
