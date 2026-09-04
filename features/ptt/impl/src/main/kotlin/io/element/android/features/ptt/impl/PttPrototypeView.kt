/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.permissions.api.PermissionsView

// NOTE: strings are hard-coded — this is a debug-only Stage 1 prototype screen and is not localised.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PttPrototypeView(
    state: PttPrototypeState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The BLE PTT button needs runtime Bluetooth permission; requesting connect + scan together shows a
    // single system dialog. The result is picked up implicitly: the presenter re-checks the grant on
    // resume (hiding the prompt) and the session host starts the BLE input source once it's available.
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                titleStr = "Push-to-talk (prototype)",
                navigationIcon = { BackButton(onClick = onBackClick) },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            // Enable/disable PTT for this room — drives the in-room header/banner/composer UI.
            ListItem(
                content = { Text("Enable push-to-talk in this room") },
                supportingContent = { Text("Shows the PTT controls in the room. (Interim: local to this device.)") },
                trailingContent = ListItemContent.Switch(
                    checked = state.isPttEnabled,
                    enabled = state.isPttAvailable,
                ),
                onClick = { state.eventSink(PttPrototypeEvent.SetPttEnabled(!state.isPttEnabled)) },
            )
            // Silent by default: audio output only exists while this is on (and covert is off).
            ListItem(
                content = { Text("Listen (hear others)") },
                supportingContent = {
                    Text(
                        if (state.isCovert) {
                            "Overridden by covert mode — the device stays silent."
                        } else {
                            "Off by default. When off, incoming audio is received but never played."
                        }
                    )
                },
                trailingContent = ListItemContent.Switch(
                    checked = state.isHearingEnabled && !state.isCovert,
                    enabled = !state.isCovert,
                ),
                onClick = { state.eventSink(PttPrototypeEvent.SetHearingEnabled(!state.isHearingEnabled)) },
            )
            // Master override — hard-silences all audio output and tones regardless of Listen.
            ListItem(
                content = { Text("Covert (silent) mode") },
                supportingContent = {
                    Text("Guarantees the device makes no sound — no audio, no tones — overriding Listen.")
                },
                trailingContent = ListItemContent.Switch(checked = state.isCovert),
                onClick = { state.eventSink(PttPrototypeEvent.SetCovertMode(!state.isCovert)) },
            )
            // Optional user opt-in — battery for background reliability.
            ListItem(
                content = { Text("Ignore battery optimizations") },
                supportingContent = {
                    Text("Improves background alert reliability on some phones. Uses more battery — optional.")
                },
                trailingContent = ListItemContent.Switch(checked = state.isIgnoringBatteryOptimizations),
                onClick = { state.eventSink(PttPrototypeEvent.ToggleBatteryOptimizationExemption) },
            )
            if (!state.canDrawOverlays) {
                ListItem(
                    content = { Text("Allow floating PTT button") },
                    supportingContent = {
                        Text("Draw a talk button over other apps to transmit while Element is in the background.")
                    },
                    onClick = { state.eventSink(PttPrototypeEvent.GrantOverlayPermission) },
                )
            }
            if (!state.canUseFullScreenIntent) {
                ListItem(
                    content = { Text("Allow full-screen PTT alerts") },
                    supportingContent = {
                        Text("Show the join screen over the lock screen when a session goes live.")
                    },
                    onClick = { state.eventSink(PttPrototypeEvent.GrantFullScreenIntent) },
                )
            }
            if (state.showBluetoothPermissionPrompt) {
                ListItem(
                    content = { Text("Allow Bluetooth for PTT buttons") },
                    supportingContent = {
                        Text("Connect to a Bluetooth push-to-talk button (e.g. Pryme) to transmit.")
                    },
                    onClick = {
                        bluetoothPermissionLauncher.launch(requiredBluetoothPermissions().toTypedArray())
                    },
                )
            }
            PttChannelStatus(state = state)
        }
        // Shows a rationale / go-to-settings dialog when the mic permission is denied.
        PermissionsView(state = state.permissionsState)
    }
}

// NOTE: strings are hard-coded — debug-only Stage 1 prototype screen, not localised.
@Composable
private fun PttChannelStatus(
    state: PttPrototypeState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val statusText = when {
            !state.isPttAvailable ->
                "No push-to-talk transport is available in this build."
            state.isTransmitting ->
                "Transmitting… (you hold the floor)"
            state.isUserInChannel ->
                "Connected to the PTT channel (${state.participantCount} participant(s)). Hold to talk."
            state.hasLiveChannel ->
                "Connecting to the PTT channel…"
            else ->
                "Not connected. Join the channel to start."
        }
        Text(
            text = statusText,
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textPrimary,
        )

        if (state.isUserInChannel) {
            // Press-and-hold: take the floor on press, release it on lift (half-duplex PTT).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        if (state.isTransmitting) {
                            ElementTheme.colors.bgActionPrimaryPressed
                        } else {
                            ElementTheme.colors.bgActionPrimaryRest
                        }
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                state.eventSink(PttPrototypeEvent.StartTransmitting)
                                tryAwaitRelease()
                                state.eventSink(PttPrototypeEvent.StopTransmitting)
                            }
                        )
                    }
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (state.isTransmitting) "Transmitting…" else "Hold to talk",
                    style = ElementTheme.typography.fontBodyLgMedium,
                    color = ElementTheme.colors.textOnSolidPrimary,
                )
            }
            Button(
                text = "Leave PTT channel",
                onClick = { state.eventSink(PttPrototypeEvent.LeavePttChannel) },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Button(
                text = "Join PTT channel",
                onClick = { state.eventSink(PttPrototypeEvent.JoinPttChannel) },
                enabled = state.isPttAvailable && !state.hasLiveChannel,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Text(
            text = "Stage 1 prototype: joins the room's PTT transport (Mumble) via the session host and " +
                "seam. Press and hold to transmit; release to stop — real half-duplex floor control " +
                "since the transport owns the microphone.",
            style = ElementTheme.typography.fontBodySmRegular,
            color = ElementTheme.colors.textSecondary,
        )
        // DEBUG: fires a full-screen "join push-to-talk" alert after 3s — lock the screen to test it.
        Button(
            text = "Simulate lock-screen alert (3s)",
            onClick = { state.eventSink(PttPrototypeEvent.SimulateLockScreenAlert) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun PttPrototypeViewPreview(
    @PreviewParameter(PttPrototypeStateProvider::class) state: PttPrototypeState
) = ElementPreview {
    PttPrototypeView(
        state = state,
        onBackClick = {},
    )
}
