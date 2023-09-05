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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.theme.colors.avatarColorsDark
import io.element.android.libraries.theme.colors.avatarColorsLight

data class AvatarColors(
    val background: Color,
    val foreground: Color,
)

@Composable
fun avatarColors(userId: String): AvatarColors {
    val hash = userId.toHash()
    val colors = if (ElementTheme.isLightTheme) {
        avatarColorsLight[hash]
    } else {
        avatarColorsDark[hash]
    }
    return AvatarColors(
        background = colors.first,
        foreground = colors.second,
    )
}

internal fun String.toHash(): Int {
    return toList().sumOf { it.code } % avatarColorsLight.size
}

