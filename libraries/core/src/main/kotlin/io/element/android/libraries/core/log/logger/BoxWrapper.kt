/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.log.logger

import kotlin.math.max

/**
 * Wraps a list of strings in a box with borders.
 * Allow to print pretty logs.
 *
 * @param minBoxInsideWidth The minimum width of the box inside, default is 80.
 * @return A list of strings representing the boxed lines.
 */
fun List<String>.wrapInBox(
    minBoxInsideWidth: Int = 80,
): List<String> {
    val maxLength = maxOfOrNull { it.length } ?: 0
    val boxWidth = max(maxLength, minBoxInsideWidth) + 4
    val topBorder = "┌" + "─".repeat(boxWidth - 2) + "┐"
    val bottomBorder = "└" + "─".repeat(boxWidth - 2) + "┘"
    val boxedLines = map { line ->
        val padding = " ".repeat(max(0, boxWidth - line.length - 4))
        "│ $line$padding │"
    }
    return listOf(topBorder) + boxedLines + listOf(bottomBorder)
}
