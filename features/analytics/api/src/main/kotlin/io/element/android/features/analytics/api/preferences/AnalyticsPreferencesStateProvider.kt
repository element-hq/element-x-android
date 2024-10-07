/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.analytics.api.preferences

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class AnalyticsPreferencesStateProvider : PreviewParameterProvider<AnalyticsPreferencesState> {
    override val values: Sequence<AnalyticsPreferencesState>
        get() = sequenceOf(
            aAnalyticsPreferencesState().copy(isEnabled = true),
        )
}

fun aAnalyticsPreferencesState() = AnalyticsPreferencesState(
    applicationName = "Element X",
    isEnabled = false,
    policyUrl = "https://element.io",
    eventSink = {}
)
