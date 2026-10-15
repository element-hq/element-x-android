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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PttSettingsView(
    state: PttSettingsState,
    onBluetoothSettingsClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(titleStr = stringResource(CommonStrings.screen_ptt_title), navigationIcon = { BackButton(onClick = onBackClick) }) },
    ) { padding ->
        Column(Modifier.padding(padding).consumeWindowInsets(padding).verticalScroll(rememberScrollState())) {
            PttToggleRow(CommonStrings.screen_ptt_covert, CommonStrings.screen_ptt_mute_all, R.drawable.ic_ptt_volume_off, state.isCovert) {
                state.eventSink(PttSettingsEvent.SetCovert(!state.isCovert))
            }
            PttToggleRow(CommonStrings.screen_ptt_overlay, CommonStrings.screen_ptt_overlay_description, R.drawable.ic_ptt_overlay, state.overlayEnabled) {
                state.eventSink(PttSettingsEvent.SetOverlay(!state.overlayEnabled))
            }
            ListItem(
                content = { Text(stringResource(CommonStrings.screen_ptt_lock_screen)) },
                supportingContent = { Text(stringResource(CommonStrings.screen_ptt_lock_screen_description)) },
                leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.LockSolid())),
                trailingContent = ListItemContent.Switch(state.lockScreenEnabled),
                onClick = { state.eventSink(PttSettingsEvent.SetLockScreen(!state.lockScreenEnabled)) },
            )
            HorizontalDivider()
            PttToggleRow(
                CommonStrings.screen_ptt_battery,
                CommonStrings.screen_ptt_battery_description,
                R.drawable.ic_ptt_battery,
                state.isIgnoringBatteryOptimizations,
            ) { state.eventSink(PttSettingsEvent.ToggleBatteryExemption) }
            if (state.bluetoothSupported) {
                ListItem(
                    content = { Text(stringResource(CommonStrings.screen_ptt_bluetooth)) },
                    supportingContent = { Text(stringResource(CommonStrings.screen_ptt_bluetooth_description)) },
                    leadingContent = ListItemContent.Icon(IconSource.Resource(R.drawable.ic_ptt_bluetooth)),
                    trailingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.ChevronRight())),
                    onClick = onBluetoothSettingsClick,
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun PttToggleRow(title: Int, description: Int, icon: Int, checked: Boolean, onClick: () -> Unit) {
    ListItem(
        content = { Text(stringResource(title)) },
        supportingContent = { Text(stringResource(description)) },
        leadingContent = ListItemContent.Icon(IconSource.Resource(icon)),
        trailingContent = ListItemContent.Switch(checked),
        onClick = onClick,
    )
    HorizontalDivider()
}

class PttSettingsStatePreviewParam : PreviewParameterProvider<PttSettingsState> {
    override val values = sequenceOf(
        PttSettingsState(
            overlayEnabled = true,
            lockScreenEnabled = true,
            isIgnoringBatteryOptimizations = true,
            bluetoothSupported = true,
            isCovert = false,
            eventSink = {},
        ),
        PttSettingsState(
            isCovert = true,
            bluetoothSupported = true,
            overlayEnabled = false,
            lockScreenEnabled = false,
            isIgnoringBatteryOptimizations = false,
            eventSink = {},
        ),
        PttSettingsState(
            isCovert = false,
            overlayEnabled = false,
            lockScreenEnabled = false,
            isIgnoringBatteryOptimizations = false,
            bluetoothSupported = false,
            eventSink = {},
        ),
    )
}

@PreviewsDayNight
@Composable
internal fun PttSettingsViewPreview(@PreviewParameter(PttSettingsStatePreviewParam::class) state: PttSettingsState) = ElementPreview {
    PttSettingsView(state, onBluetoothSettingsClick = {}, onBackClick = {})
}
