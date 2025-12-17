/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.core

import io.element.android.libraries.androidutils.metadata.isInDebug
import java.io.Serializable

@JvmInline
value class RoomAlias(val value: String) : Serializable {
    init {
        if (isInDebug && !MatrixPatterns.isRoomAlias(value)) {
            error("`$value` is not a valid room alias.\n Example room alias: `#room_alias:domain`.")
        }
    }

    override fun toString(): String = value
}
