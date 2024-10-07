/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.api.preferences

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class RageshakePreferencesStateProvider : PreviewParameterProvider<RageshakePreferencesState> {
    override val values: Sequence<RageshakePreferencesState>
        get() = sequenceOf(
            aRageshakePreferencesState().copy(isEnabled = true, isSupported = true, sensitivity = 0.5f),
            aRageshakePreferencesState().copy(isEnabled = true, isSupported = false, sensitivity = 0.5f),
        )
}

fun aRageshakePreferencesState() = RageshakePreferencesState(
    isEnabled = false,
    isSupported = true,
    sensitivity = 0.3f,
    eventSink = {}
)
