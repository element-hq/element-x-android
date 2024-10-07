/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.analytics.compose

import androidx.compose.runtime.staticCompositionLocalOf
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.noop.NoopAnalyticsService

/**
 * Global key to access the [AnalyticsService] in the composition tree.
 */
val LocalAnalyticsService = staticCompositionLocalOf<AnalyticsService> {
    NoopAnalyticsService()
}
