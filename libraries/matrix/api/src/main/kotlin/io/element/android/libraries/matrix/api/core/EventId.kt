/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.core

import io.element.android.libraries.androidutils.metadata.isInDebug
import java.io.Serializable

@JvmInline
value class EventId(val value: String) : Serializable {
    init {
        if (isInDebug && !MatrixPatterns.isEventId(value)) {
            error("`$value` is not a valid event id.\nExample event id: `\$Rqnc-F-dvnEYJTyHq_iKxU2bZ1CI92-kuZq3a5lr5Zg`.")
        }
    }

    override fun toString(): String = value
}

fun EventId.toThreadId(): ThreadId = ThreadId(value)
