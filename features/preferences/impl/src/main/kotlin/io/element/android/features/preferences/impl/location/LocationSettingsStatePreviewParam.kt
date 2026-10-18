/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.location

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class LocationSettingsStatePreviewParam : PreviewParameterProvider<LocationSettingsState> {
    override val values: Sequence<LocationSettingsState>
        get() = sequenceOf(
            aLocationSettingsState(),
            aLocationSettingsState(liveLocationMinimumDistanceUpdate = 80),
        )
}

fun aLocationSettingsState(
    liveLocationMinimumDistanceUpdate: Int = 50,
    eventSink: (LocationSettingsEvent) -> Unit = {},
) = LocationSettingsState(
    liveLocationMinimumDistanceUpdate = liveLocationMinimumDistanceUpdate,
    eventSink = eventSink
)
