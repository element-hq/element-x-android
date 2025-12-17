/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.features.roomcall.api.RoomCallStateProvider
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun CallMenuItem(
    roomCallState: RoomCallState,
    onJoinCallClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (roomCallState) {
        RoomCallState.Unavailable -> {
            Box(modifier)
        }
        is RoomCallState.StandBy -> {
            StandByCallMenuItem(
                roomCallState = roomCallState,
                onJoinCallClick = onJoinCallClick,
                modifier = modifier,
            )
        }
        is RoomCallState.OnGoing -> {
            OnGoingCallMenuItem(
                roomCallState = roomCallState,
                onJoinCallClick = onJoinCallClick,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun StandByCallMenuItem(
    roomCallState: RoomCallState.StandBy,
    onJoinCallClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier,
        onClick = onJoinCallClick,
        enabled = roomCallState.canStartCall,
    ) {
        Icon(
            imageVector = CompoundIcons.VideoCallSolid(),
            contentDescription = stringResource(CommonStrings.a11y_start_call),
        )
    }
}

@Composable
private fun OnGoingCallMenuItem(
    roomCallState: RoomCallState.OnGoing,
    onJoinCallClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!roomCallState.isUserLocallyInTheCall) {
        Button(
            onClick = onJoinCallClick,
            colors = ButtonDefaults.buttonColors(
                contentColor = ElementTheme.colors.bgCanvasDefault,
                containerColor = ElementTheme.colors.iconAccentTertiary
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            modifier = modifier.heightIn(min = 36.dp),
            enabled = roomCallState.canJoinCall,
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = CompoundIcons.VideoCallSolid(),
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(CommonStrings.action_join),
                style = ElementTheme.typography.fontBodyMdMedium
            )
            Spacer(Modifier.width(8.dp))
        }
    } else {
        // Else user is already in the call, hide the button.
        Box(modifier)
    }
}

@PreviewsDayNight
@Composable
internal fun CallMenuItemPreview(
    @PreviewParameter(RoomCallStateProvider::class) roomCallState: RoomCallState
) = ElementPreview {
    CallMenuItem(
        roomCallState = roomCallState,
        onJoinCallClick = {}
    )
}
