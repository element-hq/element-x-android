/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.api.preferences

sealed interface RageshakePreferencesEvents {
    data class SetSensitivity(val sensitivity: Float) : RageshakePreferencesEvents
    data class SetIsEnabled(val isEnabled: Boolean) : RageshakePreferencesEvents
}
