/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import io.element.android.libraries.core.data.tryOrNull
import java.text.BreakIterator

data class AvatarData(
    val id: String,
    val name: String?,
    val url: String? = null,
    val size: AvatarSize,
) {
    val initialLetter by lazy {
        // For roomIds, use "#" as initial
        (name?.takeIf { it.isNotBlank() } ?: id.takeIf { !it.startsWith("!") } ?: "#")
            .let { dn ->
                var startIndex = 0
                val initial = dn[startIndex]

                if (initial in listOf('@', '#', '+') && dn.length > 1) {
                    startIndex++
                }

                var next = dn[startIndex]

                // LEFT-TO-RIGHT MARK
                if (dn.length >= 2 && 0x200e == next.code) {
                    startIndex++
                    next = dn[startIndex]
                }

                while (next.isWhitespace()) {
                    if (dn.length > startIndex + 1) {
                        startIndex++
                        next = dn[startIndex]
                    } else {
                        break
                    }
                }

                val fullCharacterIterator = BreakIterator.getCharacterInstance()
                fullCharacterIterator.setText(dn)
                val glyphBoundary = tryOrNull { fullCharacterIterator.following(startIndex) }
                    ?.takeIf { it in startIndex..dn.length }

                when {
                    // Use the found boundary
                    glyphBoundary != null -> dn.substring(startIndex, glyphBoundary)
                    // If no boundary was found, default to the next char if possible
                    startIndex + 1 < dn.length -> dn.substring(startIndex, startIndex + 1)
                    // Return a fallback character otherwise
                    else -> "#"
                }
            }
            .uppercase()
    }
}

fun AvatarData.getBestName(): String {
    return name?.takeIf { it.isNotEmpty() } ?: id
}
