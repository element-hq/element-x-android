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
    MessageEventType.Audio -> MessageLikeEventType.Audio
    MessageEventType.Beacon -> MessageLikeEventType.Beacon
    MessageEventType.CallAnswer -> MessageLikeEventType.CallAnswer
    MessageEventType.CallCandidates -> MessageLikeEventType.CallCandidates
    MessageEventType.CallInvite -> MessageLikeEventType.CallInvite
    MessageEventType.CallHangup -> MessageLikeEventType.CallHangup
    MessageEventType.CallNegotiate -> MessageLikeEventType.CallNegotiate
    MessageEventType.CallNotify -> MessageLikeEventType.CallNotify
    MessageEventType.CallReject -> MessageLikeEventType.CallReject
    MessageEventType.CallSdpStreamMetadataChanged -> MessageLikeEventType.CallSdpStreamMetadataChanged
    MessageEventType.CallSelectAnswer -> MessageLikeEventType.CallSelectAnswer
    MessageEventType.Emote -> MessageLikeEventType.Emote
    MessageEventType.Encrypted -> MessageLikeEventType.Encrypted
    MessageEventType.File -> MessageLikeEventType.File
    MessageEventType.Image -> MessageLikeEventType.Image
    MessageEventType.KeyVerificationReady -> MessageLikeEventType.KeyVerificationReady
    MessageEventType.KeyVerificationStart -> MessageLikeEventType.KeyVerificationStart
    MessageEventType.KeyVerificationCancel -> MessageLikeEventType.KeyVerificationCancel
    MessageEventType.KeyVerificationAccept -> MessageLikeEventType.KeyVerificationAccept
    MessageEventType.KeyVerificationKey -> MessageLikeEventType.KeyVerificationKey
    MessageEventType.KeyVerificationMac -> MessageLikeEventType.KeyVerificationMac
    MessageEventType.KeyVerificationDone -> MessageLikeEventType.KeyVerificationDone
    MessageEventType.Location -> MessageLikeEventType.Location
    MessageEventType.Message -> MessageLikeEventType.Message
    MessageEventType.Reaction -> MessageLikeEventType.Reaction
    MessageEventType.RoomEncrypted -> MessageLikeEventType.RoomEncrypted
    MessageEventType.RoomMessage -> MessageLikeEventType.RoomMessage
    MessageEventType.RoomRedaction -> MessageLikeEventType.RoomRedaction
    MessageEventType.RtcDecline -> MessageLikeEventType.RtcDecline
    MessageEventType.Sticker -> MessageLikeEventType.Sticker
    MessageEventType.PollEnd -> MessageLikeEventType.PollEnd
    MessageEventType.PollResponse -> MessageLikeEventType.PollResponse
    MessageEventType.PollStart -> MessageLikeEventType.PollStart
    MessageEventType.RtcNotification -> MessageLikeEventType.RtcNotification
    MessageEventType.UnstablePollEnd -> MessageLikeEventType.UnstablePollEnd
    MessageEventType.UnstablePollResponse -> MessageLikeEventType.UnstablePollResponse
    MessageEventType.UnstablePollStart -> MessageLikeEventType.UnstablePollStart
    MessageEventType.Video -> MessageLikeEventType.Video
    MessageEventType.Voice -> MessageLikeEventType.Voice
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
    MessageLikeEventType.Audio -> MessageEventType.Audio
    MessageLikeEventType.Beacon -> MessageEventType.Beacon
    MessageLikeEventType.CallNegotiate -> MessageEventType.CallNegotiate
    MessageLikeEventType.CallNotify -> MessageEventType.CallNotify
    MessageLikeEventType.CallReject -> MessageEventType.CallReject
    MessageLikeEventType.CallSdpStreamMetadataChanged -> MessageEventType.CallSdpStreamMetadataChanged
    MessageLikeEventType.CallSelectAnswer -> MessageEventType.CallSelectAnswer
    MessageLikeEventType.Emote -> MessageEventType.Emote
    MessageLikeEventType.Encrypted -> MessageEventType.Encrypted
    MessageLikeEventType.File -> MessageEventType.File
    MessageLikeEventType.Image -> MessageEventType.Image
    MessageLikeEventType.Location -> MessageEventType.Location
    MessageLikeEventType.Message -> MessageEventType.Message
    MessageLikeEventType.RtcDecline -> MessageEventType.RtcDecline
    MessageLikeEventType.Video -> MessageEventType.Video
    MessageLikeEventType.Voice -> MessageEventType.Voice
    is MessageLikeEventType.Other -> MessageEventType.Other(v1)
}
