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

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import io.element.android.libraries.designsystem.Azure
import io.element.android.libraries.designsystem.DarkGrey
import io.element.android.libraries.designsystem.SystemGrey5Dark
import io.element.android.libraries.designsystem.SystemGrey6Dark

fun elementColorsDark() = ElementColors(
    messageFromMeBackground = SystemGrey5Dark,
    messageFromOtherBackground = SystemGrey6Dark,
    messageHighlightedBackground = Azure,
    isLight = false,
)

val materialColorSchemeDark = darkColorScheme(
    primary = Color.White,
    secondary = DarkGrey,
    tertiary = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    surfaceVariant = SystemGrey5Dark,
    onSurface = Color.White,
)
