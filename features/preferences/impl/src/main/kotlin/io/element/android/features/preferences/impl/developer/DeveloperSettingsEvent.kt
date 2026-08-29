/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import androidx.compose.ui.graphics.Color

sealed interface DeveloperSettingsEvent {
    data class SetShowColorPicker(val show: Boolean) : DeveloperSettingsEvent
    data class ChangeBrandColor(val color: Color?) : DeveloperSettingsEvent
    data object ClearCache : DeveloperSettingsEvent
    data object VacuumStores : DeveloperSettingsEvent
    data class MarkAllRoomsAsRead(val needsConfirmation: Boolean) : DeveloperSettingsEvent
    data object DismissMarkAllRoomsAsReadConfirmation : DeveloperSettingsEvent
    data object OpenPushRules : DeveloperSettingsEvent
    data object DismissPushRulesError : DeveloperSettingsEvent
}
