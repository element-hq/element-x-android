/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.api.acceptdecline

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.matrix.api.core.RoomId

/**
 * Renders the confirmation dialogs and error states of accepting or declining an invitation; it draws nothing until such an event is sent.
 *
 * Embedded by every screen that can act on an invitation, so the behaviour stays identical between the invite list and a room preview.
 */
fun interface AcceptDeclineInviteView {
    /**
     * Draws whichever dialog the current state calls for.
     *
     * @param state the state produced by the accept/decline presenter.
     * @param onAcceptInviteSuccess called once the room has been joined, so the host screen can navigate into it.
     * @param onDeclineInviteSuccess called once the invitation has been declined.
     * @param modifier layout modifier for the container.
     */
    @Composable
    fun Render(
        state: AcceptDeclineInviteState,
        onAcceptInviteSuccess: (RoomId) -> Unit,
        onDeclineInviteSuccess: (RoomId) -> Unit,
        modifier: Modifier,
    )
}
