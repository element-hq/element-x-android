/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import io.element.android.compound.theme.Theme

sealed interface AdvancedSettingsEvents {
    data class SetDeveloperModeEnabled(val enabled: Boolean) : AdvancedSettingsEvents
    data class SetSharePresenceEnabled(val enabled: Boolean) : AdvancedSettingsEvents
    data object ChangeTheme : AdvancedSettingsEvents
    data object CancelChangeTheme : AdvancedSettingsEvents
    data class SetTheme(val theme: Theme) : AdvancedSettingsEvents
}
