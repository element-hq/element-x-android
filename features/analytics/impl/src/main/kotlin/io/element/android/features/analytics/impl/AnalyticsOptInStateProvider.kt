/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.analytics.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import javax.inject.Inject

open class AnalyticsOptInStateProvider @Inject constructor() : PreviewParameterProvider<AnalyticsOptInState> {
    override val values: Sequence<AnalyticsOptInState>
        get() = sequenceOf(
            aAnalyticsOptInState(),
        )
}

fun aAnalyticsOptInState() = AnalyticsOptInState(
    applicationName = "Element X",
    eventSink = {}
)
