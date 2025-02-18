/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.runtime.Immutable
import java.text.BreakIterator

@Immutable
data class AvatarData(
    val id: String,
    val name: String?,
    val url: String? = null,
    val size: AvatarSize,
) {
    val initial by lazy {
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
                val glyphBoundary = fullCharacterIterator.following(startIndex)

                dn.substring(startIndex, glyphBoundary)
            }
            .uppercase()
    }
}

fun AvatarData.getBestName(): String {
    return name?.takeIf { it.isNotEmpty() } ?: id
}
