/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

// NOTE: strings are hard-coded — this is a debug-only Stage 1 prototype screen and is not localised.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PttPrototypeView(
    state: PttPrototypeState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                headlineContent = { Text("Enable push-to-talk in this room") },
                supportingContent = { Text("Shows the PTT controls in the room. (Interim: local to this device.)") },
                trailingContent = ListItemContent.Switch(
                    checked = state.isPttEnabled,
                    enabled = state.isPttAvailable,
                ),
                onClick = { state.eventSink(PttPrototypeEvent.SetPttEnabled(!state.isPttEnabled)) },
            )
            PttChannelStatus(state = state)
        }
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
                "Element Call (LiveKit) is not available on this homeserver — PTT cannot run here."
            state.isUserInChannel ->
                "You are live in this PTT channel (${state.participantCount} participant(s))."
            state.hasLiveChannel ->
                "PTT channel is live with ${state.participantCount} participant(s)."
            else ->
                "No one is live in this PTT channel yet."
        }
        Text(
            text = statusText,
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textPrimary,
        )
        Button(
            text = if (state.isUserInChannel) "Re-open PTT channel" else "Join PTT channel",
            onClick = { state.eventSink(PttPrototypeEvent.JoinPttChannel) },
            enabled = state.isPttAvailable,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Stage 1 prototype: joins the room's Element Call audio session as an always-on PTT " +
                "channel (instant audio, survives backgrounding/lock via the call foreground service). " +
                "The mic stays full-duplex until Element Call's controls expose a microphone toggle (Stage 2).",
            style = ElementTheme.typography.fontBodySmRegular,
            color = ElementTheme.colors.textSecondary,
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
