/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.analytics.api.preferences

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class AnalyticsPreferencesStateProvider : PreviewParameterProvider<AnalyticsPreferencesState> {
    override val values: Sequence<AnalyticsPreferencesState>
        get() = sequenceOf(
            aAnalyticsPreferencesState().copy(isEnabled = true),
            aAnalyticsPreferencesState().copy(isEnabled = true, policyUrl = ""),
        )
}

fun aAnalyticsPreferencesState(
    applicationName: String = "Element X",
    isEnabled: Boolean = false,
    policyUrl: String = "https://element.io",
) = AnalyticsPreferencesState(
    applicationName = applicationName,
    isEnabled = isEnabled,
    policyUrl = policyUrl,
    eventSink = {}
)
