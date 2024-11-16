/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.livelocation

import androidx.compose.runtime.Composable
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import javax.inject.Inject

class LiveLocationPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val sessionPreferencesStore: SessionPreferencesStore,
) : Presenter<LiveLocationState> {
    @Composable
    override fun present(): LiveLocationState {
        return TODO("Provide the return value")
    }

//    private fun ProduceStateScope<ImmutableList<LiveLocationRoomMember>>.observeLiveLocationShares() {
//        combine(room.liveLocationShareFlow, room.membersStateFlow) { locationShares, membersState ->
//            locationShares
//                .map { userId ->
//                    membersState.roomMembers()
//                        ?.firstOrNull { roomMember -> roomMember.userId == userId }
//                        ?.toTypingRoomMember()
//                        ?: createDefaultRoomMemberForTyping(userId)
//                }
//        }
//            .distinctUntilChanged()
//            .onEach { members ->
//                value = members.toImmutableList()
//            }
//            .launchIn(this)
//    }
}
