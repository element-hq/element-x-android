/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.leaveroom.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.leaveroom.api.LeaveRoomRenderer
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId

@ContributesBinding(SessionScope::class)
class InternalLeaveRoomRenderer : LeaveRoomRenderer {
    @Composable
    override fun Render(state: LeaveRoomState, onSelectNewOwners: (RoomId) -> Unit, modifier: Modifier) {
        if (state is InternalLeaveRoomState) {
            LeaveRoomView(state, onSelectNewOwners)
        } else {
            error("Unsupported state type ${state.javaClass}")
        }
    }
}
