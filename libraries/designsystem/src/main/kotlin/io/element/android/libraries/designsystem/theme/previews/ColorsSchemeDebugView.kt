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

package io.element.android.libraries.designsystem.theme.previews

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.persistentMapOf

@Composable
internal fun ColorsSchemeDebugView(
    backgroundColor: Color,
    foregroundColor: Color,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
) {
    val colors = persistentMapOf(
        "primary" to colorScheme.primary,
        "onPrimary" to colorScheme.onPrimary,
        "primaryContainer" to colorScheme.primaryContainer,
        "onPrimaryContainer" to colorScheme.onPrimaryContainer,
        "inversePrimary" to colorScheme.inversePrimary,
        "secondary" to colorScheme.secondary,
        "onSecondary" to colorScheme.onSecondary,
        "secondaryContainer" to colorScheme.secondaryContainer,
        "onSecondaryContainer" to colorScheme.onSecondaryContainer,
        "tertiary" to colorScheme.tertiary,
        "onTertiary" to colorScheme.onTertiary,
        "tertiaryContainer" to colorScheme.tertiaryContainer,
        "onTertiaryContainer" to colorScheme.onTertiaryContainer,
        "background" to colorScheme.background,
        "onBackground" to colorScheme.onBackground,
        "surface" to colorScheme.surface,
        "onSurface" to colorScheme.onSurface,
        "surfaceVariant" to colorScheme.surfaceVariant,
        "onSurfaceVariant" to colorScheme.onSurfaceVariant,
        "surfaceTint" to colorScheme.surfaceTint,
        "inverseSurface" to colorScheme.inverseSurface,
        "inverseOnSurface" to colorScheme.inverseOnSurface,
        "error" to colorScheme.error,
        "onError" to colorScheme.onError,
        "errorContainer" to colorScheme.errorContainer,
        "onErrorContainer" to colorScheme.onErrorContainer,
        "outline" to colorScheme.outline,
        "outlineVariant" to colorScheme.outlineVariant,
        "scrim" to colorScheme.scrim,
    )
    ColorListDebugView(
        backgroundColor = backgroundColor,
        foregroundColor = foregroundColor,
        colors = colors,
        modifier = modifier,
    )
}
