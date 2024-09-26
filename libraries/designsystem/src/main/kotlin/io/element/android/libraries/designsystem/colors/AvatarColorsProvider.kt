/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.colors

import androidx.compose.runtime.Composable
import io.element.android.compound.theme.AvatarColors
import io.element.android.compound.theme.avatarColors

object AvatarColorsProvider {
    @Composable
    fun provide(id: String): AvatarColors {
        return avatarColors().let { colors ->
            colors[id.toHash(colors.size)]
        }
    }
}

internal fun String.toHash(maxSize: Int): Int {
    return toList().sumOf { it.code } % maxSize
}
