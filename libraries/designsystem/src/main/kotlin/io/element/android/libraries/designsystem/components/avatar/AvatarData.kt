/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.runtime.Immutable
import fr.gouv.tchap.libraries.tchaputils.TchapPatterns.toUserDisplayName

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
                var first = dn[startIndex]

                // LEFT-TO-RIGHT MARK
                if (dn.length >= 2 && 0x200e == first.code) {
                    startIndex++
                    first = dn[startIndex]
                }

                // check if it’s the start of a surrogate pair
                if (first.code in 0xD800..0xDBFF && dn.length > startIndex + 1) {
                    val second = dn[startIndex + 1]
                    if (second.code in 0xDC00..0xDFFF) {
                        length++
                    }
                }

                dn.substring(startIndex, startIndex + length)
            }
            .uppercase()
    }
}

fun AvatarData.getBestName(): String {
    return name?.takeIf { it.isNotEmpty() } ?: id.toUserDisplayName()
}
