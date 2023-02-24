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

package io.element.android.libraries.designsystem.theme.compound

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.element.android.libraries.designsystem.SystemDark
import io.element.android.libraries.designsystem.SystemLight

interface CompoundColorPalette {
    interface Content {
        val primary: Color
        val secondary: Color
        val tertiary: Color
        val quaternary: Color
        val quinary: Color
    }

    val content: Content

    val system: Color

    object Light : CompoundColorPalette {
        object LightContent: Content {
            override val primary: Color = Color(0xFF17191C)
            override val secondary: Color = Color(0xFF737D8C)
            override val tertiary: Color = Color(0xFF8D97A5)
            override val quaternary: Color = Color(0xFFC1C6CD)
            override val quinary: Color = Color(0xFFE3E8F0)
        }

        override val content: Content = LightContent

        override val system: Color = SystemLight
    }

    object Dark : CompoundColorPalette {
        object DarkContent: Content {
            override val primary: Color = Color(0xFFFFFFFF)
            override val secondary: Color = Color(0xFFA9B2BC)
            override val tertiary: Color = Color(0xFF8E99A4)
            override val quaternary: Color = Color(0xFF6F7882)
            override val quinary: Color = Color(0xFF394049)
        }

        override val content: Content = DarkContent

        override val system: Color = SystemDark
    }
}


val LocalCompoundColors = staticCompositionLocalOf<CompoundColorPalette> { CompoundColorPalette.Light }
