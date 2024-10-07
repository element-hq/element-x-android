/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.analytics.impl

import io.element.android.features.analytics.api.AnalyticsOptInEvents

data class AnalyticsOptInState(
    val applicationName: String,
    val eventSink: (AnalyticsOptInEvents) -> Unit
)
