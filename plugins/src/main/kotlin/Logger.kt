/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

import org.gradle.api.logging.Logger
import kotlin.math.max

fun Logger.warnInBox(
    text: String,
    minBoxWidth: Int = 80,
    padding: Int = 4,
) {
    val textLength = text.length
    val boxWidth = max(textLength + 2, minBoxWidth)
    val textPadding = max((boxWidth - textLength) / 2, 1)
    warn(
        buildString {
            append(" ".repeat(padding))
            append("┌")
            append("─".repeat(boxWidth))
            append("┐")
        }
    )
    warn(
        buildString {
            append(" ".repeat(padding))
            append("│")
            append(" ".repeat(textPadding))
            append(text)
            append(" ".repeat(textPadding))
            if (textLength % 2 == 1 && boxWidth == minBoxWidth) append(" ")
            append("│")
        }
    )
    warn(
        buildString {
            append(" ".repeat(padding))
            append("└")
            append("─".repeat(boxWidth))
            append("┘")
        }
    )
}
