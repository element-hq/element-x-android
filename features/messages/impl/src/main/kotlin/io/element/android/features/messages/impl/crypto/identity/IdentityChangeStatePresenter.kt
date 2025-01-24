/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.identity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.messages.impl.messagecomposer.observeRoomMemberIdentityStateChange
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.ui.model.getAvatarData
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
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
            observeRoomMemberIdentityStateChange(room)
        }

        fun handleEvent(event: IdentityChangeEvent) {
            when (event) {
                is IdentityChangeEvent.WithdrawVerification -> {
                    coroutineScope.withdrawVerification(event.userId)
                }
                is IdentityChangeEvent.PinIdentity -> {
                    coroutineScope.pinUserIdentity(event.userId)
                }
            }
        }

        return IdentityChangeState(
            roomMemberIdentityStateChanges = roomMemberIdentityStateChange,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.pinUserIdentity(userId: UserId) = launch {
        encryptionService.pinUserIdentity(userId)
            .onFailure {
                Timber.e(it, "Failed to pin identity for user $userId")
            }
    }

    private fun CoroutineScope.withdrawVerification(userId: UserId) = launch {
        encryptionService.withdrawVerification(userId)
            .onFailure {
                Timber.e(it, "Failed to withdraw verification for user $userId")
            }
    }
}

fun RoomMember.toIdentityRoomMember() = IdentityRoomMember(
    userId = userId,
    displayNameOrDefault = displayNameOrDefault,
    avatarData = getAvatarData(AvatarSize.ComposerAlert),
)

fun createDefaultRoomMemberForIdentityChange(userId: UserId) = IdentityRoomMember(
    userId = userId,
    displayNameOrDefault = userId.extractedDisplayName,
    avatarData = AvatarData(
        id = userId.value,
        name = null,
        url = null,
        size = AvatarSize.ComposerAlert,
    ),
)
