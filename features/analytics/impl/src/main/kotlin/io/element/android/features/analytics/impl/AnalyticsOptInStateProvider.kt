/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.analytics.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import javax.inject.Inject

open class AnalyticsOptInStateProvider @Inject constructor() : PreviewParameterProvider<AnalyticsOptInState> {
    override val values: Sequence<AnalyticsOptInState>
        get() = sequenceOf(
            aAnalyticsOptInState(),
            aAnalyticsOptInState(hasPolicyLink = false),
        )
}

fun aAnalyticsOptInState(
    hasPolicyLink: Boolean = true,
) = AnalyticsOptInState(
    applicationName = "Element X",
    hasPolicyLink = hasPolicyLink,
    eventSink = {}
)
