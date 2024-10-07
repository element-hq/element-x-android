/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.di

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Provides a [TimelineItemPresenterFactories] to the composition.
 */
val LocalTimelineItemPresenterFactories = staticCompositionLocalOf {
    TimelineItemPresenterFactories(emptyMap())
}
