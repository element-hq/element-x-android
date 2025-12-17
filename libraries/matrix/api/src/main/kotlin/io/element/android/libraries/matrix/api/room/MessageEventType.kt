/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import androidx.compose.runtime.Immutable

@Immutable
sealed interface MessageEventType {
    data object CallAnswer : MessageEventType
    data object CallInvite : MessageEventType
    data object CallHangup : MessageEventType
    data object CallCandidates : MessageEventType
    data object RtcNotification : MessageEventType
    data object KeyVerificationReady : MessageEventType
    data object KeyVerificationStart : MessageEventType
    data object KeyVerificationCancel : MessageEventType
    data object KeyVerificationAccept : MessageEventType
    data object KeyVerificationKey : MessageEventType
    data object KeyVerificationMac : MessageEventType
    data object KeyVerificationDone : MessageEventType
    data object Reaction : MessageEventType
    data object RoomEncrypted : MessageEventType
    data object RoomMessage : MessageEventType
    data object RoomRedaction : MessageEventType
    data object Sticker : MessageEventType
    data object PollEnd : MessageEventType
    data object PollResponse : MessageEventType
    data object PollStart : MessageEventType
    data object UnstablePollEnd : MessageEventType
    data object UnstablePollResponse : MessageEventType
    data object UnstablePollStart : MessageEventType
    data class Other(val type: String) : MessageEventType
}
