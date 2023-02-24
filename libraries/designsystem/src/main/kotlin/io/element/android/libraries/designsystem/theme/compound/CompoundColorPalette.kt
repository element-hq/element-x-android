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
import io.element.android.libraries.designsystem.Black_800
import io.element.android.libraries.designsystem.Black_900
import io.element.android.libraries.designsystem.Black_950
import io.element.android.libraries.designsystem.Gray_100
import io.element.android.libraries.designsystem.Gray_150
import io.element.android.libraries.designsystem.Gray_200
import io.element.android.libraries.designsystem.Gray_25
import io.element.android.libraries.designsystem.Gray_250
import io.element.android.libraries.designsystem.Gray_300
import io.element.android.libraries.designsystem.Gray_400
import io.element.android.libraries.designsystem.Gray_450
import io.element.android.libraries.designsystem.Gray_50

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
    val background: Color

    object Light : CompoundColorPalette {
        object LightContent: Content {
            override val primary: Color = Black_900
            override val secondary: Color = Gray_200
            override val tertiary: Color = Gray_150
            override val quaternary: Color = Gray_100
            override val quinary: Color = Gray_50
        }

        override val content: Content = LightContent

        override val system: Color = Gray_25
        override val background: Color = Color.White
    }

    object Dark : CompoundColorPalette {
        object DarkContent: Content {
            override val primary: Color = Color.White
            override val secondary: Color = Gray_250
            override val tertiary: Color = Gray_300
            override val quaternary: Color = Gray_400
            override val quinary: Color = Gray_450
        }

        override val content: Content = DarkContent

        override val system: Color = Black_950
        override val background: Color = Black_800
    }
}


val LocalCompoundColors = staticCompositionLocalOf<CompoundColorPalette> { CompoundColorPalette.Light }
