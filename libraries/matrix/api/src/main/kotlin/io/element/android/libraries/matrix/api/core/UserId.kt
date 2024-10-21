/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.core

import io.element.android.libraries.androidutils.metadata.isInDebug
import java.io.Serializable

/**
 * A [String] holding a valid Matrix user ID.
 *
 * https://spec.matrix.org/v1.8/appendices/#user-identifiers
 */
@JvmInline
value class UserId(val value: String) : Serializable {
    init {
        if (isInDebug && !MatrixPatterns.isUserId(value)) {
            error("`$value` is not a valid user id.\nExample user id: `@name:domain`.")
        }
    }

    override fun toString(): String = value

    val extractedDisplayName: String
        get() = value
            .removePrefix("@")
            .substringBefore(":")
}
