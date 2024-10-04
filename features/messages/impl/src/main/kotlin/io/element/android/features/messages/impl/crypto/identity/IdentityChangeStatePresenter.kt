/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.identity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.roomMembers
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class IdentityChangeStatePresenter @Inject constructor(
    private val room: MatrixRoom,
    private val encryptionService: EncryptionService,
) : Presenter<IdentityChangeState> {
    @Composable
    override fun present(): IdentityChangeState {
        val coroutineScope = rememberCoroutineScope()
        val roomMemberIdentityStateChange = remember {
            mutableStateOf(persistentListOf<RoomMemberIdentityStateChange>())
        }

        LaunchedEffect(Unit) {
            observeRoomMemberIdentityStateChange(roomMemberIdentityStateChange)
        }

        fun handleEvent(event: IdentityChangeEvent) {
            when (event) {
                is IdentityChangeEvent.Submit -> coroutineScope.pinUserIdentity(event.userId)
            }
        }

        return IdentityChangeState(
            roomMemberIdentityStateChanges = roomMemberIdentityStateChange.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.observeRoomMemberIdentityStateChange(roomMemberIdentityStateChange: MutableState<PersistentList<RoomMemberIdentityStateChange>>) {
        combine(room.identityStateChangesFlow, room.membersStateFlow) { IdentityStateChanges, membersState ->
            IdentityStateChanges.map { identityStateChange ->
                val member = membersState.roomMembers()
                    ?.firstOrNull { roomMember -> roomMember.userId == identityStateChange.userId }
                    ?: createDefaultRoomMemberForIdentityChange(identityStateChange.userId)
                RoomMemberIdentityStateChange(
                    roomMember = member,
                    identityState = identityStateChange.identityState,
                )
            }
        }
            .distinctUntilChanged()
            .onEach { roomMemberIdentityStateChanges ->
                roomMemberIdentityStateChange.value = roomMemberIdentityStateChanges.toPersistentList()
            }
            .launchIn(this)
    }

    private fun CoroutineScope.pinUserIdentity(userId: UserId) = launch {
        encryptionService.pinUserIdentity(userId)
            .onFailure {
                Timber.e(it, "Failed to pin identity for user $userId")
            }
    }
}

/**
 * Create a default [RoomMember] for identity change events.
 * In this case, only the userId will be used for rendering, other fields are not used, but keep them
 * as close as possible to the actual data.
 */
private fun createDefaultRoomMemberForIdentityChange(userId: UserId): RoomMember {
    return RoomMember(
        userId = userId,
        displayName = null,
        avatarUrl = null,
        membership = RoomMembershipState.JOIN,
        isNameAmbiguous = false,
        powerLevel = 0,
        normalizedPowerLevel = 0,
        isIgnored = false,
        role = RoomMember.Role.USER,
    )
}
