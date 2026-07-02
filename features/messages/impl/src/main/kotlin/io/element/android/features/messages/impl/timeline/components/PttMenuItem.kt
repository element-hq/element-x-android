/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.features.roomcall.api.RoomCallStateProvider
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton

/**
 * Push-to-Talk header control that REPLACES the call button in PTT-enabled rooms. Three states,
 * mapped onto [RoomCallState]:
 * - StandBy → start a session (bare walkie-talkie icon)
 * - OnGoing, not locally joined → join (the session banner also offers Join)
 * - OnGoing, locally joined → leave (crossed-out walkie-talkie)
 *
 * Audio-only, no video. NOTE: leaving a joined session has no non-UI path yet (see roadmap plan);
 * [onLeaveClick] currently re-opens the call screen so the user can hang up there.
 */
@Composable
internal fun PttMenuItem(
    roomCallState: RoomCallState,
    onStartOrJoinClick: () -> Unit,
    onLeaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (roomCallState) {
        RoomCallState.Unavailable -> {
            Box(modifier)
        }
        is RoomCallState.StandBy -> {
            IconButton(
                onClick = onStartOrJoinClick,
                enabled = roomCallState.canStartCall,
                modifier = modifier,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_ptt),
                    // TODO localise once the prototype graduates.
                    contentDescription = "Start push-to-talk session",
                )
            }
        }
        is RoomCallState.OnGoing -> {
            if (roomCallState.isUserLocallyInTheCall) {
                // TODO Figma shows a filled pill; leaving needs call-infra (currently re-opens the call).
                IconButton(
                    onClick = onLeaveClick,
                    modifier = modifier,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_ptt_off),
                        contentDescription = "Leave push-to-talk session",
                    )
                }
            } else {
                // TODO Figma shows an outlined pill; the session banner carries the primary Join.
                IconButton(
                    onClick = onStartOrJoinClick,
                    enabled = roomCallState.canJoinCall,
                    modifier = modifier,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_ptt),
                        contentDescription = "Join push-to-talk session",
                    )
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun PttMenuItemPreview(
    @PreviewParameter(RoomCallStateProvider::class) roomCallState: RoomCallState
) = ElementPreview {
    PttMenuItem(
        roomCallState = roomCallState,
        onStartOrJoinClick = {},
        onLeaveClick = {},
    )
}
