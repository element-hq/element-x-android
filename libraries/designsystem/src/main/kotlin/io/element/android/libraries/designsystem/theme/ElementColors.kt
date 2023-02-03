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
    messageFromMeBackground: Color,
    messageFromOtherBackground: Color,
    messageHighlightedBackground: Color,
    isLight: Boolean,
) {
    var messageFromMeBackground by mutableStateOf(messageFromMeBackground)
        private set
    var messageFromOtherBackground by mutableStateOf(messageFromOtherBackground)
        private set
    var messageHighlightedBackground by mutableStateOf(messageHighlightedBackground)
        private set

    var isLight by mutableStateOf(isLight)
        private set

    fun copy(
        messageFromMeBackground: Color = this.messageFromMeBackground,
        messageFromOtherBackground: Color = this.messageFromOtherBackground,
        messageHighlightedBackground: Color = this.messageHighlightedBackground,
        isLight: Boolean = this.isLight,
    ) = ElementColors(
        messageFromMeBackground = messageFromMeBackground,
        messageFromOtherBackground = messageFromOtherBackground,
        messageHighlightedBackground = messageHighlightedBackground,
        isLight = isLight,
    )

    fun updateColorsFrom(other: ElementColors) {
        messageFromMeBackground = other.messageFromMeBackground
        messageFromOtherBackground = other.messageFromOtherBackground
        messageHighlightedBackground = other.messageHighlightedBackground
        isLight = other.isLight
    }
}
