/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.leaveroom.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.matrix.api.core.RoomId

/**
 * Renders the confirmation dialogs of the leave room flow; it draws nothing until the host screen sends a leave event.
 */
fun interface LeaveRoomRenderer {
    /**
     * Draws whichever dialog the current state calls for.
     *
     * @param state the state produced by the leave room presenter.
     * @param onSelectNewOwners called when the user is the last owner of a room that still has other members, and must promote someone before leaving.
     * @param modifier layout modifier for the container.
     */
    @Composable
    fun Render(
        state: LeaveRoomState,
        onSelectNewOwners: (RoomId) -> Unit,
        modifier: Modifier,
    )
}
