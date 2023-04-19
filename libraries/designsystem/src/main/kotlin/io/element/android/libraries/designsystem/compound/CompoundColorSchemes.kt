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

package io.element.android.libraries.designsystem.compound

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

val compoundColorsLight = lightColorScheme(
    // Used for normal text color and buttons background
    primary = LightDesignTokens.colorBgActionPrimaryRest,
    // Used for icons and text inside components using Primary color for their background
    onPrimary = LightDesignTokens.colorTextOnSolidPrimary,
    // Used for secondary text and icons mainly
    secondary = LightDesignTokens.colorIconSecondary,
    // Used for the background of most of the screens in the application
    background = LightDesignTokens.colorBgCanvasDefault,
    onBackground = LightDesignTokens.colorIconSecondary,
    // Used for components such as navigation drawers or bottom sheets
    surface = LightDesignTokens.colorBgCanvasDefault,
    // Used for text and icons by default inside components using Surface color for their background
    onSurface = LightDesignTokens.colorIconSecondary,
    // Used for dialog backgrounds
    surfaceVariant = LightDesignTokens.colorBgCanvasDefault,
    // Used for error tint in texts and icons
    error = LightDesignTokens.colorTextCriticalPrimary,
    // Used for the border of non-selected text fields, i.e
    outline = LightDesignTokens.colorGray500,
)

val compoundColorsDark = darkColorScheme(
    // Used for normal text color and buttons background
    primary = DarkDesignTokens.colorBgActionPrimaryRest,
    // Used for icons and text inside components using Primary color for their background
    onPrimary = DarkDesignTokens.colorTextOnSolidPrimary,
    // Used for secondary text and icons mainly
    secondary = DarkDesignTokens.colorIconSecondary,
    // Used for the background of most of the screens in the application
    background = DarkDesignTokens.colorBgCanvasDefault,
    onBackground = DarkDesignTokens.colorIconSecondary,
    // Used for components such as navigation drawers or bottom sheets
    surface = DarkDesignTokens.colorBgCanvasDefault,
    // Used for text and icons by default inside components using Surface color for their background
    onSurface = DarkDesignTokens.colorIconSecondary,
    // Used for dialog backgrounds
    surfaceVariant = DarkDesignTokens.colorBgCanvasDefault,
    // Used for error tint in texts and icons
    error = DarkDesignTokens.colorTextCriticalPrimary,
    // Used for the border of non-selected text fields, i.e
    outline = DarkDesignTokens.colorGray500,
)
