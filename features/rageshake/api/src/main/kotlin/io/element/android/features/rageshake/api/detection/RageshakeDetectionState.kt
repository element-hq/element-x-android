/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.detection

import androidx.compose.runtime.Immutable
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesState

@Immutable
data class RageshakeDetectionState(
    val takeScreenshot: Boolean,
    val showDialog: Boolean,
    val isStarted: Boolean,
    val preferenceState: RageshakePreferencesState,
    val eventSink: (RageshakeDetectionEvents) -> Unit
)
