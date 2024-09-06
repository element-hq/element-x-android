/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.members.moderation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationPresenter
import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationState

class FakeRoomMembersModerationPresenter(
    private val canDisplayModerationActions: Boolean = true,
) : RoomMembersModerationPresenter {
    private var state by mutableStateOf(dummyState())

    override suspend fun canDisplayModerationActions(): Boolean {
        return canDisplayModerationActions
    }

    @Composable
    override fun present(): RoomMembersModerationState {
        return state
    }

    fun givenState(state: RoomMembersModerationState) {
        this.state = state
    }
}
