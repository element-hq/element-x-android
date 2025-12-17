/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.preview

import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.utils.CommonDrawables

@OptIn(ExperimentalCoilApi::class)
@Composable
@Suppress("ModifierMissing")
fun ElementPreview(
    darkTheme: Boolean = isSystemInDarkTheme(),
    showBackground: Boolean = true,
    @DrawableRes
    drawableFallbackForImages: Int = CommonDrawables.sample_background,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    CompositionLocalProvider(
        LocalAsyncImagePreviewHandler provides AsyncImagePreviewHandler {
            ResourcesCompat.getDrawable(context.resources, drawableFallbackForImages, null)!!.asImage()
        }
    ) {
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
