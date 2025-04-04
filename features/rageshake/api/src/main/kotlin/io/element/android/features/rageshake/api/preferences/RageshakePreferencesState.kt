/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.preferences

data class RageshakePreferencesState(
    val isFeatureEnabled: Boolean,
    val isEnabled: Boolean,
    val isSupported: Boolean,
    val sensitivity: Float,
    val eventSink: (RageshakePreferencesEvents) -> Unit,
)
