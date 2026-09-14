/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Whether formatted message bodies should be rendered with the native Compose renderer
 * (see [io.element.android.libraries.htmlrenderer.impl.renderer.HtmlMessageContent]) instead of the
 * legacy rich text editor view. Provided by [TimelineView] from the timeline feature flag; defaults
 * to false so any surface that doesn't provide it keeps the previous behaviour.
 */
val LocalUseNewTimelineEventRenderer = staticCompositionLocalOf { false }
