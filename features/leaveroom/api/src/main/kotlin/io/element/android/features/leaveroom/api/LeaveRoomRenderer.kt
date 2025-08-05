/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.leaveroom.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.matrix.api.core.RoomId

interface LeaveRoomRenderer {
    @Composable
    fun Render(
        state: LeaveRoomState,
        onSelectNewOwners: (RoomId) -> Unit,
        modifier: Modifier,
    )
}
