/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.ptt.api.PttBluetoothStatus
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PttBluetoothSettingsView(state: PttBluetoothSettingsState, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    var confirmForget by rememberSaveable { mutableStateOf(false) }
    val device = state.device
    val busy = device.status == PttBluetoothStatus.Selecting || device.status == PttBluetoothStatus.Connecting
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(titleStr = stringResource(CommonStrings.screen_ptt_button_title), navigationIcon = { BackButton(onClick = onBackClick) }) },
    ) { padding ->
        Column(Modifier.padding(padding).consumeWindowInsets(padding).verticalScroll(rememberScrollState())) {
            ListItem(
                content = { Text(device.deviceName ?: stringResource(CommonStrings.screen_ptt_button_none)) },
                supportingContent = { Text(stringResource(device.status.description())) },
            )
            HorizontalDivider()
            if (device.deviceName != null) {
                val connected = device.status == PttBluetoothStatus.Ready || busy
                ListItem(
                    content = { Text(stringResource(if (connected) CommonStrings.screen_ptt_button_disconnect else CommonStrings.screen_ptt_button_connect)) },
                    onClick = { state.eventSink(if (connected) PttBluetoothSettingsEvent.Disconnect else PttBluetoothSettingsEvent.Connect) },
                )
                ListItem(
                    content = { Text(stringResource(CommonStrings.screen_ptt_button_auto_connect)) },
                    supportingContent = { Text(stringResource(CommonStrings.screen_ptt_button_auto_connect_description)) },
                    trailingContent = ListItemContent.Switch(device.autoConnect),
                    onClick = { state.eventSink(PttBluetoothSettingsEvent.SetAutoConnect(!device.autoConnect)) },
                )
                HorizontalDivider()
            }
            ListItem(
                content = {
                    Text(stringResource(if (device.deviceName == null) CommonStrings.screen_ptt_button_find else CommonStrings.screen_ptt_button_change))
                },
                supportingContent = { Text(stringResource(CommonStrings.screen_ptt_button_find_description)) },
                enabled = !busy,
                onClick = { state.eventSink(PttBluetoothSettingsEvent.Find) },
            )
            if (device.status == PttBluetoothStatus.Ready) {
                HorizontalDivider()
                ListItem(
                    content = {
                        Text(stringResource(if (device.isPressed) CommonStrings.screen_ptt_button_pressed else CommonStrings.screen_ptt_button_released))
                    },
                    supportingContent = { Text(stringResource(CommonStrings.screen_ptt_button_test_description, device.eventCount)) },
                )
            }
            if (device.status == PttBluetoothStatus.PermissionRequired) {
                ListItem(
                    content = { Text(stringResource(CommonStrings.screen_ptt_app_permissions)) },
                    onClick = { state.eventSink(PttBluetoothSettingsEvent.OpenAppSettings) },
                )
            }
            if (device.status == PttBluetoothStatus.BluetoothOff) {
                ListItem(
                    content = { Text(stringResource(CommonStrings.screen_ptt_bluetooth_open_settings)) },
                    onClick = { state.eventSink(PttBluetoothSettingsEvent.OpenBluetoothSettings) },
                )
            }
            if (device.deviceName != null) {
                HorizontalDivider()
                ListItem(
                    content = { Text(stringResource(CommonStrings.screen_ptt_button_forget)) },
                    onClick = { confirmForget = true },
                )
            }
        }
    }
    if (confirmForget) {
        ConfirmationDialog(
            title = stringResource(CommonStrings.screen_ptt_button_forget),
            content = stringResource(CommonStrings.screen_ptt_button_forget_description),
            submitText = stringResource(CommonStrings.screen_ptt_button_forget),
            onSubmitClick = {
                confirmForget = false
                state.eventSink(PttBluetoothSettingsEvent.Forget)
            },
            onDismiss = { confirmForget = false },
        )
    }
}

private fun PttBluetoothStatus.description(): Int = when (this) {
    PttBluetoothStatus.Disconnected -> CommonStrings.screen_ptt_button_disconnected
    PttBluetoothStatus.Selecting -> CommonStrings.screen_ptt_button_selecting
    PttBluetoothStatus.Connecting -> CommonStrings.screen_ptt_button_connecting
    PttBluetoothStatus.Ready -> CommonStrings.screen_ptt_button_ready
    PttBluetoothStatus.PermissionRequired -> CommonStrings.screen_ptt_button_permission
    PttBluetoothStatus.BluetoothOff -> CommonStrings.screen_ptt_button_bluetooth_off
    PttBluetoothStatus.Unsupported -> CommonStrings.screen_ptt_button_unsupported
    PttBluetoothStatus.Failed -> CommonStrings.screen_ptt_button_failed
}

@PreviewsDayNight
@Composable
internal fun PttBluetoothSettingsViewPreview(
    @PreviewParameter(PttBluetoothSettingsStatePreviewParam::class) state: PttBluetoothSettingsState,
) = ElementPreview {
    PttBluetoothSettingsView(state, onBackClick = {})
}
