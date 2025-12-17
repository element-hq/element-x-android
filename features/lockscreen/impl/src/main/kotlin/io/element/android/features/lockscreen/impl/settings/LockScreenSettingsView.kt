/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.lockscreen.impl.R
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceDivider
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun LockScreenSettingsView(
    state: LockScreenSettingsState,
    onChangePinClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferencePage(
        title = stringResource(id = io.element.android.libraries.ui.strings.R.string.common_screen_lock),
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        PreferenceCategory(showTopDivider = false) {
            ListItem(
                headlineContent = {
                    Text(stringResource(id = R.string.screen_app_lock_settings_change_pin))
                },
                onClick = onChangePinClick,
            )
            PreferenceDivider()
            if (state.showRemovePinOption) {
                ListItem(
                    headlineContent = {
                        Text(stringResource(id = R.string.screen_app_lock_settings_remove_pin))
                    },
                    style = ListItemStyle.Destructive,
                    onClick = {
                        state.eventSink(LockScreenSettingsEvents.OnRemovePin)
                    }
                )
            }
            if (state.showToggleBiometric) {
                PreferenceDivider()
                PreferenceSwitch(
                    title = stringResource(id = R.string.screen_app_lock_settings_enable_biometric_unlock),
                    isChecked = state.isBiometricEnabled,
                    onCheckedChange = {
                        state.eventSink(LockScreenSettingsEvents.ToggleBiometricAllowed)
                    }
                )
            }
        }
    }
    if (state.showRemovePinConfirmation) {
        ConfirmationDialog(
            title = stringResource(id = R.string.screen_app_lock_settings_remove_pin_alert_title),
            content = stringResource(id = R.string.screen_app_lock_settings_remove_pin_alert_message),
            onSubmitClick = {
                state.eventSink(LockScreenSettingsEvents.ConfirmRemovePin)
            },
            onDismiss = {
                state.eventSink(LockScreenSettingsEvents.CancelRemovePin)
            }
        )
    }
}

@PreviewsDayNight
@Composable
internal fun LockScreenSettingsViewPreview(
    @PreviewParameter(LockScreenSettingsStateProvider::class) state: LockScreenSettingsState,
) {
    ElementPreview {
        LockScreenSettingsView(
            state = state,
            onChangePinClick = {},
            onBackClick = {},
        )
    }
}
