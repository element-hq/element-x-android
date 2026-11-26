/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * Sets up everything the timeline rows need to render at the configured text size.
 */
@Composable
fun ProvideTimelineEventRendererConfig(
    config: ComposeLocalTimelineEventRendererConfig,
    content: @Composable () -> Unit,
) {
    // Only the font scales: `dp` values keep the layout as designed.
    val density = LocalDensity.current
    val scaledDensity = remember(density, config.fontScale) {
        Density(density.density, density.fontScale * config.fontScale)
    }
    CompositionLocalProvider(
        LocalTimelineEventRendererConfig provides config,
        LocalDensity provides scaledDensity,
        content = content,
    )
}
