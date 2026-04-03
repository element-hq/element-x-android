/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.slashcommands.impl.rainbow

data class RgbColor(
    val r: Int,
    val g: Int,
    val b: Int
)

fun RgbColor.toDashColor(): String {
    return listOf(r, g, b)
        .joinToString(separator = "", prefix = "#") {
            it.toString(16).padStart(2, '0')
        }
}
