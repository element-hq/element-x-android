/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members.moderation

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.persistentListOf

interface RoomMembersModerationPresenter : Presenter<RoomMembersModerationState> {
    suspend fun canDisplayModerationActions(): Boolean

    fun dummyState() = RoomMembersModerationState(
        selectedRoomMember = null,
        actions = persistentListOf(),
        kickUserAsyncAction = AsyncAction.Uninitialized,
        banUserAsyncAction = AsyncAction.Uninitialized,
        unbanUserAsyncAction = AsyncAction.Uninitialized,
        canDisplayBannedUsers = false,
        eventSink = {}
    )
}
