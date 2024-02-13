/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
