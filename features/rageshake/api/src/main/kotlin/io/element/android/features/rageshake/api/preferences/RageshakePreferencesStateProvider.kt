/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.preferences

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class RageshakePreferencesStateProvider : PreviewParameterProvider<RageshakePreferencesState> {
    override val values: Sequence<RageshakePreferencesState>
        get() = sequenceOf(
            aRageshakePreferencesState(isEnabled = true, isSupported = true, sensitivity = 0.5f),
            aRageshakePreferencesState(isEnabled = true, isSupported = false, sensitivity = 0.5f),
        )
}

fun aRageshakePreferencesState(
    isFeatureEnabled: Boolean = true,
    isEnabled: Boolean = false,
    isSupported: Boolean = true,
    sensitivity: Float = 0.3f,
    eventSink: (RageshakePreferencesEvents) -> Unit = {}
) = RageshakePreferencesState(
    isFeatureEnabled = isFeatureEnabled,
    isEnabled = isEnabled,
    isSupported = isSupported,
    sensitivity = sensitivity,
    eventSink = eventSink,
)
