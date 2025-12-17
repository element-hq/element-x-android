/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.errors.FocusEventException
import org.matrix.rustcomponents.sdk.FocusEventException as RustFocusEventException

fun Throwable.toFocusEventException(): Throwable {
    return when (this) {
        is RustFocusEventException -> {
            when (this) {
                is RustFocusEventException.InvalidEventId -> {
                    FocusEventException.InvalidEventId(eventId, err)
                }
                is RustFocusEventException.EventNotFound -> {
                    FocusEventException.EventNotFound(EventId(eventId))
                }
                is RustFocusEventException.Other -> {
                    FocusEventException.Other(msg)
                }
            }
        }
        else -> {
            this
        }
    }
}
