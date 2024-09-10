/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.colors

import androidx.collection.LruCache
import io.element.android.compound.theme.AvatarColors
import io.element.android.compound.theme.avatarColorsDark
import io.element.android.compound.theme.avatarColorsLight

object AvatarColorsProvider {
    private val cache = LruCache<String, AvatarColors>(200)
    private var currentThemeIsLight = true

    fun provide(id: String, isLightTheme: Boolean): AvatarColors {
        if (currentThemeIsLight != isLightTheme) {
            currentThemeIsLight = isLightTheme
            cache.evictAll()
        }
        val valueFromCache = cache.get(id)
        return if (valueFromCache != null) {
            valueFromCache
        } else {
            val colors = avatarColors(id, isLightTheme)
            cache.put(id, colors)
            colors
        }
    }

    private fun avatarColors(id: String, isLightTheme: Boolean): AvatarColors {
        val hash = id.toHash()
        val colors = if (isLightTheme) {
            avatarColorsLight[hash]
        } else {
            avatarColorsDark[hash]
        }
        return colors
    }
}

internal fun String.toHash(): Int {
    return toList().sumOf { it.code } % avatarColorsLight.size
}
