/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.analytics

import io.element.android.features.analytics.api.preferences.AnalyticsPreferencesState

// Do not use default value, so no member get forgotten in the presenters.
data class AnalyticsSettingsState(
    val analyticsPreferencesState: AnalyticsPreferencesState,
)
