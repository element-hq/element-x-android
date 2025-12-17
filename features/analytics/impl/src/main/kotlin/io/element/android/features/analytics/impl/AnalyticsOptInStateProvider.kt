/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.analytics.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class AnalyticsOptInStateProvider : PreviewParameterProvider<AnalyticsOptInState> {
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
