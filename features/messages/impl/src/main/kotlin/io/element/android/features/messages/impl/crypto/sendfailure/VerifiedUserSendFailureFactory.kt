/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.sendfailure

import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import javax.inject.Inject

class VerifiedUserSendFailureFactory @Inject constructor(
    private val room: MatrixRoom,
) {
    suspend fun create(
        sendState: LocalEventSendState?,
    ): VerifiedUserSendFailure {
        return when (sendState) {
            is LocalEventSendState.Failed.VerifiedUserHasUnsignedDevice -> {
                val userId = sendState.devices.keys.firstOrNull()
                if (userId == null) {
                    VerifiedUserSendFailure.None
                } else {
                    val displayName = room.userDisplayName(userId).getOrNull() ?: userId.value
                    VerifiedUserSendFailure.UnsignedDevice(displayName)
                }
            }
            is LocalEventSendState.Failed.VerifiedUserChangedIdentity -> {
                val userId = sendState.users.firstOrNull()
                if (userId == null) {
                    VerifiedUserSendFailure.None
                } else {
                    val displayName = room.userDisplayName(userId).getOrNull() ?: userId.value
                    VerifiedUserSendFailure.ChangedIdentity(displayName)
                }
            }
            else -> VerifiedUserSendFailure.None
        }
    }
}
