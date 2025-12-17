/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.pin.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PinDigit {
    data object Empty : PinDigit
    data class Filled(val value: Char) : PinDigit

    fun toText(): String {
        return when (this) {
            is Empty -> ""
            is Filled -> value.toString()
        }
    }
}
