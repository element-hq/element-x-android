/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.staticCompositionLocalOf
import io.element.android.libraries.matrix.api.core.UserId

/**
 * Local configuration for the timeline event renderer, so it can be accessed from UI components without having to pass it down through parameters.
 */
val LocalTimelineEventRendererConfig = staticCompositionLocalOf {
    ComposeLocalTimelineEventRendererConfig(
        isComposeRendererEnabled = false,
        currentUserId = null,
    )
}

/**
 * Configuration for the timeline event renderer.
 */
data class ComposeLocalTimelineEventRendererConfig(
    /** Whether the compose renderer is enabled or not. If `false` (the default option), the view-based renderer will be used instead. */
    val isComposeRendererEnabled: Boolean,
    /** The current user ID, if known. This can be used to display UI sent by/owned by the current user differently. */
    val currentUserId: UserId?,
)
