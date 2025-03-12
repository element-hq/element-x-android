/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.features.userprofile.api.UserProfileEvents
import io.element.android.features.userprofile.api.UserProfilePresenterFactory
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.features.userprofile.api.UserProfileVerificationState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.encryption.identity.IdentityStateChange
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.ui.room.getRoomMemberAsState
import io.element.android.libraries.matrix.ui.room.roomMemberIdentityStateChange
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Presenter for room member details screen.
 * Rely on UserProfilePresenter, but override some fields with room member info when available.
 */
class RoomMemberDetailsPresenter @AssistedInject constructor(
    @Assisted private val roomMemberId: UserId,
    private val room: MatrixRoom,
    private val encryptionService: EncryptionService,
    userProfilePresenterFactory: UserProfilePresenterFactory,
) : Presenter<UserProfileState> {
    interface Factory {
        fun create(roomMemberId: UserId): RoomMemberDetailsPresenter
    }

    private val userProfilePresenter = userProfilePresenterFactory.create(roomMemberId)

    @Composable
    override fun present(): UserProfileState {
        val coroutineScope = rememberCoroutineScope()

        val roomMember by room.getRoomMemberAsState(roomMemberId)
        LaunchedEffect(Unit) {
            // Update room member info when opening this screen
            // We don't need to assign the result as it will be automatically propagated by `room.getRoomMemberAsState`
            room.getUpdatedMember(roomMemberId)
        }

        val roomUserName: String? by produceState(
            initialValue = roomMember?.displayName,
            key1 = roomMember,
        ) {
            value = room.userDisplayName(roomMemberId).getOrNull() ?: roomMember?.displayName
        }

        val roomUserAvatar: String? by produceState(
            initialValue = roomMember?.avatarUrl,
            key1 = roomMember,
        ) {
            value = room.userAvatarUrl(roomMemberId).getOrNull() ?: roomMember?.avatarUrl
        }

        val userProfileState = userProfilePresenter.present()

        val identityStateChanges by produceState<IdentityStateChange?>(initialValue = null) {
            if (room.isEncrypted) {
                // Fetch the initial identity state manually
                val identityState = encryptionService.getUserIdentity(roomMemberId).getOrNull()
                value = identityState?.let { IdentityStateChange(roomMemberId, it) }

                // Subscribe to the identity changes
                room.roomMemberIdentityStateChange()
                    .map { it.find { it.identityRoomMember.userId == roomMemberId } }
                    .map { roomMemberIdentityStateChange ->
                        // If we didn't receive any info, manually fetch it
                        roomMemberIdentityStateChange?.identityState ?: encryptionService.getUserIdentity(roomMemberId).getOrNull()
                    }
                    .filterNotNull()
                    .collect { value = IdentityStateChange(roomMemberId, it) }
            }
        }

        val verificationState = remember(identityStateChanges) {
            when (identityStateChanges?.identityState) {
                IdentityState.VerificationViolation -> UserProfileVerificationState.VERIFICATION_VIOLATION
                IdentityState.Verified -> UserProfileVerificationState.VERIFIED
                IdentityState.Pinned, IdentityState.PinViolation -> UserProfileVerificationState.UNVERIFIED
                else -> UserProfileVerificationState.UNKNOWN
            }
        }

        fun eventSink(event: UserProfileEvents) {
            when (event) {
                UserProfileEvents.WithdrawVerification -> coroutineScope.launch {
                    encryptionService.withdrawVerification(roomMemberId)
                }
                else -> Unit
            }
        }

        return userProfileState.copy(
            userName = roomUserName ?: userProfileState.userName,
            avatarUrl = roomUserAvatar ?: userProfileState.avatarUrl,
            verificationState = verificationState,
            eventSink = { event ->
                if (event is UserProfileEvents.WithdrawVerification) {
                    eventSink(UserProfileEvents.WithdrawVerification)
                } else {
                    userProfileState.eventSink(event)
                }
            }
        )
    }
}
