/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.leaveroom.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.leaveroom.api.LeaveRoomRenderer
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class InternalLeaveRoomRenderer @Inject constructor() : LeaveRoomRenderer {
    @Composable
    override fun Render(state: LeaveRoomState, onSelectNewOwners: (RoomId) -> Unit, modifier: Modifier) {
        if (state is InternalLeaveRoomState) {
            LeaveRoomView(state, onSelectNewOwners)
        } else {
            error("Unsupported state type ${state.javaClass}")
        }
    }
}
