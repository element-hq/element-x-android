/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * Showkase does not take into account the `fontScale` parameter of the Preview annotation, so alter the
 * LocalDensity in the CompositionLocalProvider.
 */
@Composable
fun WithFontScale(fontScale: Float, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = LocalDensity.current.density,
            fontScale = fontScale
        )
    ) {
        content()
    }
}
