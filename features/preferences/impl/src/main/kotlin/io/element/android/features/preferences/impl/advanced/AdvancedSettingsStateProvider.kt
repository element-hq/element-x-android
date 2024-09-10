/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.compound.theme.Theme

open class AdvancedSettingsStateProvider : PreviewParameterProvider<AdvancedSettingsState> {
    override val values: Sequence<AdvancedSettingsState>
        get() = sequenceOf(
            aAdvancedSettingsState(),
            aAdvancedSettingsState(isDeveloperModeEnabled = true),
            aAdvancedSettingsState(showChangeThemeDialog = true),
            aAdvancedSettingsState(isSendPublicReadReceiptsEnabled = true),
        )
}

fun aAdvancedSettingsState(
    isDeveloperModeEnabled: Boolean = false,
    isSendPublicReadReceiptsEnabled: Boolean = false,
    showChangeThemeDialog: Boolean = false,
    eventSink: (AdvancedSettingsEvents) -> Unit = {},
) = AdvancedSettingsState(
    isDeveloperModeEnabled = isDeveloperModeEnabled,
    isSharePresenceEnabled = isSendPublicReadReceiptsEnabled,
    theme = Theme.System,
    showChangeThemeDialog = showChangeThemeDialog,
    eventSink = eventSink
)
