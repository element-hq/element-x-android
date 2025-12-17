/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.room.MessageEventType
import org.matrix.rustcomponents.sdk.MessageLikeEventType

fun MessageEventType.map(): MessageLikeEventType = when (this) {
    MessageEventType.CallAnswer -> MessageLikeEventType.CallAnswer
    MessageEventType.CallInvite -> MessageLikeEventType.CallInvite
    MessageEventType.CallHangup -> MessageLikeEventType.CallHangup
    MessageEventType.CallCandidates -> MessageLikeEventType.CallCandidates
    MessageEventType.RtcNotification -> MessageLikeEventType.RtcNotification
    MessageEventType.KeyVerificationReady -> MessageLikeEventType.KeyVerificationReady
    MessageEventType.KeyVerificationStart -> MessageLikeEventType.KeyVerificationStart
    MessageEventType.KeyVerificationCancel -> MessageLikeEventType.KeyVerificationCancel
    MessageEventType.KeyVerificationAccept -> MessageLikeEventType.KeyVerificationAccept
    MessageEventType.KeyVerificationKey -> MessageLikeEventType.KeyVerificationKey
    MessageEventType.KeyVerificationMac -> MessageLikeEventType.KeyVerificationMac
    MessageEventType.KeyVerificationDone -> MessageLikeEventType.KeyVerificationDone
    MessageEventType.Reaction -> MessageLikeEventType.Reaction
    MessageEventType.RoomEncrypted -> MessageLikeEventType.RoomEncrypted
    MessageEventType.RoomMessage -> MessageLikeEventType.RoomMessage
    MessageEventType.RoomRedaction -> MessageLikeEventType.RoomRedaction
    MessageEventType.Sticker -> MessageLikeEventType.Sticker
    MessageEventType.PollEnd -> MessageLikeEventType.PollEnd
    MessageEventType.PollResponse -> MessageLikeEventType.PollResponse
    MessageEventType.PollStart -> MessageLikeEventType.PollStart
    MessageEventType.UnstablePollEnd -> MessageLikeEventType.UnstablePollEnd
    MessageEventType.UnstablePollResponse -> MessageLikeEventType.UnstablePollResponse
    MessageEventType.UnstablePollStart -> MessageLikeEventType.UnstablePollStart
    is MessageEventType.Other -> MessageLikeEventType.Other(type)
}

fun MessageLikeEventType.map(): MessageEventType = when (this) {
    MessageLikeEventType.CallAnswer -> MessageEventType.CallAnswer
    MessageLikeEventType.CallInvite -> MessageEventType.CallInvite
    MessageLikeEventType.CallHangup -> MessageEventType.CallHangup
    MessageLikeEventType.CallCandidates -> MessageEventType.CallCandidates
    MessageLikeEventType.RtcNotification -> MessageEventType.RtcNotification
    MessageLikeEventType.KeyVerificationReady -> MessageEventType.KeyVerificationReady
    MessageLikeEventType.KeyVerificationStart -> MessageEventType.KeyVerificationStart
    MessageLikeEventType.KeyVerificationCancel -> MessageEventType.KeyVerificationCancel
    MessageLikeEventType.KeyVerificationAccept -> MessageEventType.KeyVerificationAccept
    MessageLikeEventType.KeyVerificationKey -> MessageEventType.KeyVerificationKey
    MessageLikeEventType.KeyVerificationMac -> MessageEventType.KeyVerificationMac
    MessageLikeEventType.KeyVerificationDone -> MessageEventType.KeyVerificationDone
    MessageLikeEventType.Reaction -> MessageEventType.Reaction
    MessageLikeEventType.RoomEncrypted -> MessageEventType.RoomEncrypted
    MessageLikeEventType.RoomMessage -> MessageEventType.RoomMessage
    MessageLikeEventType.RoomRedaction -> MessageEventType.RoomRedaction
    MessageLikeEventType.Sticker -> MessageEventType.Sticker
    MessageLikeEventType.PollEnd -> MessageEventType.PollEnd
    MessageLikeEventType.PollResponse -> MessageEventType.PollResponse
    MessageLikeEventType.PollStart -> MessageEventType.PollStart
    MessageLikeEventType.UnstablePollEnd -> MessageEventType.UnstablePollEnd
    MessageLikeEventType.UnstablePollResponse -> MessageEventType.UnstablePollResponse
    MessageLikeEventType.UnstablePollStart -> MessageEventType.UnstablePollStart
    is MessageLikeEventType.Other -> MessageEventType.Other(v1)
}
