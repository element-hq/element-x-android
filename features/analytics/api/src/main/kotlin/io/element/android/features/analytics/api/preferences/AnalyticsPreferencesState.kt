/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.analytics.api.preferences

import io.element.android.features.analytics.api.AnalyticsOptInEvents

data class AnalyticsPreferencesState(
    val applicationName: String,
    val isEnabled: Boolean,
    val policyUrl: String,
    val eventSink: (AnalyticsOptInEvents) -> Unit,
)
