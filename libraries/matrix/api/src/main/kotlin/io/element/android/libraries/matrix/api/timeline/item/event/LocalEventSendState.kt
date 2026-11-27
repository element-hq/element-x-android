/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

@Immutable
sealed interface LocalEventSendState {
    sealed interface Sending : LocalEventSendState {
        data object Event : Sending
        data class MediaWithProgress(
            val index: Long,
            val progress: Long,
            val total: Long
        ) : Sending
    }
    sealed interface Failed : LocalEventSendState {
        /**
         * Whether the error is considered recoverable or not.
         * A recoverable error disables the room's send queue, while an unrecoverable error
         * is parked until the event is retried or aborted.
         */
        val isRecoverable: Boolean

        data class Unknown(
            val error: String,
            override val isRecoverable: Boolean,
        ) : Failed

        data class SendingFromUnverifiedDevice(
            override val isRecoverable: Boolean,
        ) : Failed

        sealed interface VerifiedUser : Failed
        data class VerifiedUserHasUnsignedDevice(
            /**
             * The unsigned devices belonging to verified users. A map from user ID
             * to a list of device IDs.
             */
            val devices: Map<UserId, List<DeviceId>>,
            override val isRecoverable: Boolean,
        ) : VerifiedUser

        data class VerifiedUserChangedIdentity(
            /**
             * The users that were previously verified but are no longer.
             */
            val users: List<UserId>,
            override val isRecoverable: Boolean,
        ) : VerifiedUser

        data class InvalidMimeType(
            val mimeType: String,
            override val isRecoverable: Boolean,
        ) : Failed

        data class MissingMediaContent(
            override val isRecoverable: Boolean,
        ) : Failed
    }

    data class Sent(
        val eventId: EventId
    ) : LocalEventSendState
}
