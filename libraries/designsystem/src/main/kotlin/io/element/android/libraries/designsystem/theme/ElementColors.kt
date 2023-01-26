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

package io.element.android.libraries.designsystem.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

class ElementColors(
    primary: Color,
    onPrimary: Color,
    secondary: Color,
    text: Color,
    background: Color,
    onBackground: Color,
    surfaceVariant: Color,
    onSurfaceVariant: Color,
    messageFromMeBackground: Color,
    messageFromOtherBackground: Color,
    messageHighlightedBackground: Color,
    success: Color,
    error: Color,
    isLight: Boolean,
) {
    var primary by mutableStateOf(primary)
        private set
    var onPrimary by mutableStateOf(onPrimary)
        private set
    var secondary by mutableStateOf(secondary)
        private set
    var text by mutableStateOf(text)
        private set
    var success by mutableStateOf(success)
        private set
    var error by mutableStateOf(error)
        private set
    var background by mutableStateOf(background)
        private set
    var onBackground by mutableStateOf(onBackground)
        private set
    var surfaceVariant by mutableStateOf(surfaceVariant)
        private set
    var onSurfaceVariant by mutableStateOf(onSurfaceVariant)
        private set
    var messageFromMeBackground by mutableStateOf(messageFromMeBackground)
        private set
    var messageFromOtherBackground by mutableStateOf(messageFromOtherBackground)
        private set
    var messageHighlightedBackground by mutableStateOf(messageHighlightedBackground)
        private set

    var isLight by mutableStateOf(isLight)
        private set

    fun copy(
        primary: Color = this.primary,
        onPrimary: Color = this.onPrimary,
        secondary: Color = this.secondary,
        text: Color = this.text,
        background: Color = this.background,
        onBackground: Color = this.onBackground,
        surfaceVariant: Color = this.surfaceVariant,
        onSurfaceVariant: Color = this.onSurfaceVariant,
        messageFromMeBackground: Color = this.messageFromMeBackground,
        messageFromOtherBackground: Color = this.messageFromOtherBackground,
        messageHighlightedBackground: Color = this.messageHighlightedBackground,
        success: Color = this.success,
        error: Color = this.error,
        isLight: Boolean = this.isLight,
    ) = ElementColors(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        text = text,
        background = background,
        onBackground = onBackground,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        messageFromMeBackground = messageFromMeBackground,
        messageFromOtherBackground = messageFromOtherBackground,
        messageHighlightedBackground = messageHighlightedBackground,
        success = success,
        error = error,
        isLight = isLight,
    )

    fun updateColorsFrom(other: ElementColors) {
        primary = other.primary
        onPrimary = other.onPrimary
        secondary = other.secondary
        text = other.text
        success = other.success
        background = other.background
        onBackground = other.onBackground
        surfaceVariant = other.surfaceVariant
        onSurfaceVariant = other.onSurfaceVariant
        messageFromMeBackground = other.messageFromMeBackground
        messageFromOtherBackground = other.messageFromOtherBackground
        messageHighlightedBackground = other.messageHighlightedBackground
        error = other.error
        isLight = other.isLight
    }
}
