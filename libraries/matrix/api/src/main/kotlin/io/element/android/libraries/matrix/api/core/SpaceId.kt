/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.core

import io.element.android.libraries.androidutils.metadata.isInDebug
import java.io.Serializable

@JvmInline
value class SpaceId(val value: String) : Serializable {
    init {
        if (isInDebug && !MatrixPatterns.isSpaceId(value)) {
            error(
                "`$value` is not a valid space id.\n" +
                    "Space ids are the same as room ids.\n" +
                    "Example space id: `!space_id:domain`."
            )
        }
    }

    override fun toString(): String = value
}

/**
 * Value to use when no space is selected by the user.
 */
val MAIN_SPACE = SpaceId("!mainSpace:local")
