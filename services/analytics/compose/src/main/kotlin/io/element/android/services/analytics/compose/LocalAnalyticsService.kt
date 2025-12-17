/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
