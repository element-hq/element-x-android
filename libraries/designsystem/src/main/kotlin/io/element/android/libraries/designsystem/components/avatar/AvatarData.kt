/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.runtime.Immutable

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

                var length = 1
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

                // check if it’s the start of a surrogate pair
                while (isPartOfEmoji(next) && dn.length > startIndex + length) {
                    length++
                    if (dn.length > startIndex + length + 1) {
                        next = dn[startIndex + length]
                    } else {
                        break
                    }
                }

                dn.substring(startIndex, startIndex + length)
            }
            .uppercase()
    }
}

fun AvatarData.getBestName(): String {
    return name?.takeIf { it.isNotEmpty() } ?: id
}

private fun isPartOfEmoji(char: Char): Boolean {
    val isHighSurrogate = char.code in 0xD800..0xDBFF
    val isLowSurrogate = char.code in 0xDC00..0xDFFF
    val isVariantSelector = char.code in 0xFE00..0xFE0F
    val isZeroWidthJoin = char.code == 0x200D
    return isHighSurrogate || isLowSurrogate || isVariantSelector || isZeroWidthJoin
}
