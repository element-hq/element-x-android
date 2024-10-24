/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

@Immutable
sealed interface LocalEventSendState {
    data object Sending : LocalEventSendState
    sealed interface Failed : LocalEventSendState {
        data class Unknown(val error: String) : Failed
        data object SendingFromUnverifiedDevice : Failed

        sealed interface VerifiedUser : Failed
        data class VerifiedUserHasUnsignedDevice(
            /**
             * The unsigned devices belonging to verified users. A map from user ID
             * to a list of device IDs.
             */
            val devices: Map<UserId, List<DeviceId>>
        ) : VerifiedUser

        data class VerifiedUserChangedIdentity(
            /**
             * The users that were previously verified but are no longer.
             */
            val users: List<UserId>
        ) : VerifiedUser
    }

    data class Sent(
        val eventId: EventId
    ) : LocalEventSendState
}
