/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Surface

@OptIn(ExperimentalCoilApi::class)
@Composable
@Suppress("ModifierMissing")
fun ElementPreview(
    darkTheme: Boolean = isSystemInDarkTheme(),
    showBackground: Boolean = true,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides AsyncImagePreviewHandler { null }) {
        ElementTheme(darkTheme = darkTheme) {
            if (showBackground) {
                // If we have a proper contentColor applied we need a Surface instead of a Box
                Surface(content = content)
            } else {
                content()
            }
        }
    }
}
