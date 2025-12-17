/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.typing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProduceStateScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Inject
class TypingNotificationPresenter(
    private val room: JoinedRoom,
    private val sessionPreferencesStore: SessionPreferencesStore,
) : Presenter<TypingNotificationState> {
    @Composable
    override fun present(): TypingNotificationState {
        val renderTypingNotifications by remember {
            sessionPreferencesStore.isRenderTypingNotificationsEnabled()
        }.collectAsState(initial = true)
        val typingMembersState by produceState(initialValue = persistentListOf(), key1 = renderTypingNotifications) {
            if (renderTypingNotifications) {
                observeRoomTypingMembers()
            } else {
                value = persistentListOf<TypingRoomMember>()
            }
        }

        // This will keep the space reserved for the typing notifications after the first one is displayed
        var reserveSpace by remember { mutableStateOf(false) }
        LaunchedEffect(renderTypingNotifications, typingMembersState) {
            if (renderTypingNotifications && typingMembersState.isNotEmpty()) {
                reserveSpace = true
            }
        }

        return TypingNotificationState(
            renderTypingNotifications = renderTypingNotifications,
            typingMembers = typingMembersState,
            reserveSpace = reserveSpace,
        )
    }

    private fun ProduceStateScope<ImmutableList<TypingRoomMember>>.observeRoomTypingMembers() {
        combine(room.roomTypingMembersFlow, room.membersStateFlow) { typingMembers, membersState ->
            typingMembers
                .map { userId ->
                    membersState.roomMembers()
                        ?.firstOrNull { roomMember -> roomMember.userId == userId }
                        ?.toTypingRoomMember()
                        ?: createDefaultRoomMemberForTyping(userId)
                }
        }
            .distinctUntilChanged()
            .onEach { members ->
                value = members.toImmutableList()
            }
            .launchIn(this)
    }
}

private fun RoomMember.toTypingRoomMember(): TypingRoomMember {
    return TypingRoomMember(
        disambiguatedDisplayName = disambiguatedDisplayName,
    )
}

private fun createDefaultRoomMemberForTyping(userId: UserId): TypingRoomMember {
    return TypingRoomMember(
        disambiguatedDisplayName = userId.value,
    )
}
