/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton

/**
 * Push-to-Talk header control that REPLACES the call button in PTT-enabled rooms.
 *
 * Independent of Element Call: shown whenever PTT is enabled, toggling between "start a session" and
 * "leave" based on the local session state ([isInSession]) rather than an Element Call room-call
 * state. Audio-only.
 */
@Composable
internal fun PttMenuItem(
    isInSession: Boolean,
    onStartOrJoinClick: () -> Unit,
    onLeaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isInSession) {
        IconButton(
            onClick = onLeaveClick,
            modifier = modifier,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_ptt_off),
                // TODO localise once the prototype graduates.
                contentDescription = "Leave push-to-talk session",
            )
        }
    } else {
        IconButton(
            onClick = onStartOrJoinClick,
            modifier = modifier,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_ptt),
                contentDescription = "Start push-to-talk session",
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun PttMenuItemStartPreview() = ElementPreview {
    PttMenuItem(isInSession = false, onStartOrJoinClick = {}, onLeaveClick = {})
}

@PreviewsDayNight
@Composable
internal fun PttMenuItemInSessionPreview() = ElementPreview {
    PttMenuItem(isInSession = true, onStartOrJoinClick = {}, onLeaveClick = {})
}
