/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.identity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProduceStateScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.ui.model.getAvatarData
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
        val roomMemberIdentityStateChange by produceState(persistentListOf()) {
            observeRoomMemberIdentityStateChange()
        }

        fun handleEvent(event: IdentityChangeEvent) {
            when (event) {
                is IdentityChangeEvent.Submit -> coroutineScope.pinUserIdentity(event.userId)
            }
        }

        return IdentityChangeState(
            roomMemberIdentityStateChanges = roomMemberIdentityStateChange,
            eventSink = ::handleEvent,
        )
    }

    private fun ProduceStateScope<PersistentList<RoomMemberIdentityStateChange>>.observeRoomMemberIdentityStateChange() {
        combine(room.identityStateChangesFlow, room.membersStateFlow) { identityStateChanges, membersState ->
            identityStateChanges.map { identityStateChange ->
                val member = membersState.roomMembers()
                    ?.firstOrNull { roomMember -> roomMember.userId == identityStateChange.userId }
                    ?.toIdentityRoomMember()
                    ?: createDefaultRoomMemberForIdentityChange(identityStateChange.userId)
                RoomMemberIdentityStateChange(
                    identityRoomMember = member,
                    identityState = identityStateChange.identityState,
                )
            }
        }
            .distinctUntilChanged()
            .onEach { roomMemberIdentityStateChanges ->
                value = roomMemberIdentityStateChanges.toPersistentList()
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

private fun RoomMember.toIdentityRoomMember() = IdentityRoomMember(
    userId = userId,
    disambiguatedDisplayName = disambiguatedDisplayName,
    avatarData = getAvatarData(AvatarSize.ComposerAlert),
)

private fun createDefaultRoomMemberForIdentityChange(userId: UserId) = IdentityRoomMember(
    userId = userId,
    disambiguatedDisplayName = userId.value,
    avatarData = AvatarData(
        id = userId.value,
        name = null,
        url = null,
        size = AvatarSize.ComposerAlert,
    ),
)
