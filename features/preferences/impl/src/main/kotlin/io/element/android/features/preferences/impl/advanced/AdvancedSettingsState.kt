/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import io.element.android.compound.theme.Theme

data class AdvancedSettingsState(
    val isDeveloperModeEnabled: Boolean,
    val isSharePresenceEnabled: Boolean,
    val theme: Theme,
    val showChangeThemeDialog: Boolean,
    val eventSink: (AdvancedSettingsEvents) -> Unit
)
