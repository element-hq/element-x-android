/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.number.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface Digit {
    data object Empty : Digit
    data class Filled(val value: Char) : Digit

    fun toText(): String {
        return when (this) {
            is Empty -> ""
            is Filled -> value.toString()
        }
    }
}
