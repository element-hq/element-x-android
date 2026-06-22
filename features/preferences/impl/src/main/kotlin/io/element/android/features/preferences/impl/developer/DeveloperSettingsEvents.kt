/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import androidx.compose.ui.graphics.Color

sealed interface DeveloperSettingsEvents {
    data class SetShowColorPicker(val show: Boolean) : DeveloperSettingsEvents
    data class ChangeBrandColor(val color: Color?) : DeveloperSettingsEvents
    data object ClearCache : DeveloperSettingsEvents
    data object VacuumStores : DeveloperSettingsEvents
}
