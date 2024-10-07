/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.analytics

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.analytics.api.preferences.aAnalyticsPreferencesState

open class AnalyticsSettingsStateProvider : PreviewParameterProvider<AnalyticsSettingsState> {
    override val values: Sequence<AnalyticsSettingsState>
        get() = sequenceOf(
            aAnalyticsSettingsState(),
        )
}

fun aAnalyticsSettingsState() = AnalyticsSettingsState(
    analyticsPreferencesState = aAnalyticsPreferencesState(),
)
