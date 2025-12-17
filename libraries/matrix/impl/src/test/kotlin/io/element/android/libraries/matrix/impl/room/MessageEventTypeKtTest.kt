/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.MessageEventType
import org.junit.Test
import org.matrix.rustcomponents.sdk.MessageLikeEventType

class MessageEventTypeKtTest {
    @Test
    fun `map Rust type should result to correct Kotlin type`() {
        assertThat(MessageLikeEventType.CallAnswer.map()).isEqualTo(MessageEventType.CallAnswer)
        assertThat(MessageLikeEventType.CallInvite.map()).isEqualTo(MessageEventType.CallInvite)
        assertThat(MessageLikeEventType.CallHangup.map()).isEqualTo(MessageEventType.CallHangup)
        assertThat(MessageLikeEventType.CallCandidates.map()).isEqualTo(MessageEventType.CallCandidates)
        assertThat(MessageLikeEventType.RtcNotification.map()).isEqualTo(MessageEventType.RtcNotification)
        assertThat(MessageLikeEventType.KeyVerificationReady.map()).isEqualTo(MessageEventType.KeyVerificationReady)
        assertThat(MessageLikeEventType.KeyVerificationStart.map()).isEqualTo(MessageEventType.KeyVerificationStart)
        assertThat(MessageLikeEventType.KeyVerificationCancel.map()).isEqualTo(MessageEventType.KeyVerificationCancel)
        assertThat(MessageLikeEventType.KeyVerificationAccept.map()).isEqualTo(MessageEventType.KeyVerificationAccept)
        assertThat(MessageLikeEventType.KeyVerificationKey.map()).isEqualTo(MessageEventType.KeyVerificationKey)
        assertThat(MessageLikeEventType.KeyVerificationMac.map()).isEqualTo(MessageEventType.KeyVerificationMac)
        assertThat(MessageLikeEventType.KeyVerificationDone.map()).isEqualTo(MessageEventType.KeyVerificationDone)
        assertThat(MessageLikeEventType.Reaction.map()).isEqualTo(MessageEventType.Reaction)
        assertThat(MessageLikeEventType.RoomEncrypted.map()).isEqualTo(MessageEventType.RoomEncrypted)
        assertThat(MessageLikeEventType.RoomMessage.map()).isEqualTo(MessageEventType.RoomMessage)
        assertThat(MessageLikeEventType.RoomRedaction.map()).isEqualTo(MessageEventType.RoomRedaction)
        assertThat(MessageLikeEventType.Sticker.map()).isEqualTo(MessageEventType.Sticker)
        assertThat(MessageLikeEventType.PollEnd.map()).isEqualTo(MessageEventType.PollEnd)
        assertThat(MessageLikeEventType.PollResponse.map()).isEqualTo(MessageEventType.PollResponse)
        assertThat(MessageLikeEventType.PollStart.map()).isEqualTo(MessageEventType.PollStart)
        assertThat(MessageLikeEventType.UnstablePollEnd.map()).isEqualTo(MessageEventType.UnstablePollEnd)
        assertThat(MessageLikeEventType.UnstablePollResponse.map()).isEqualTo(MessageEventType.UnstablePollResponse)
        assertThat(MessageLikeEventType.UnstablePollStart.map()).isEqualTo(MessageEventType.UnstablePollStart)
    }

    @Test
    fun `map Kotlin type should result to correct Rust type`() {
        assertThat(MessageEventType.CallAnswer.map()).isEqualTo(MessageLikeEventType.CallAnswer)
        assertThat(MessageEventType.CallInvite.map()).isEqualTo(MessageLikeEventType.CallInvite)
        assertThat(MessageEventType.CallHangup.map()).isEqualTo(MessageLikeEventType.CallHangup)
        assertThat(MessageEventType.CallCandidates.map()).isEqualTo(MessageLikeEventType.CallCandidates)
        assertThat(MessageEventType.RtcNotification.map()).isEqualTo(MessageLikeEventType.RtcNotification)
        assertThat(MessageEventType.KeyVerificationReady.map()).isEqualTo(MessageLikeEventType.KeyVerificationReady)
        assertThat(MessageEventType.KeyVerificationStart.map()).isEqualTo(MessageLikeEventType.KeyVerificationStart)
        assertThat(MessageEventType.KeyVerificationCancel.map()).isEqualTo(MessageLikeEventType.KeyVerificationCancel)
        assertThat(MessageEventType.KeyVerificationAccept.map()).isEqualTo(MessageLikeEventType.KeyVerificationAccept)
        assertThat(MessageEventType.KeyVerificationKey.map()).isEqualTo(MessageLikeEventType.KeyVerificationKey)
        assertThat(MessageEventType.KeyVerificationMac.map()).isEqualTo(MessageLikeEventType.KeyVerificationMac)
        assertThat(MessageEventType.KeyVerificationDone.map()).isEqualTo(MessageLikeEventType.KeyVerificationDone)
        assertThat(MessageEventType.Reaction.map()).isEqualTo(MessageLikeEventType.Reaction)
        assertThat(MessageEventType.RoomEncrypted.map()).isEqualTo(MessageLikeEventType.RoomEncrypted)
        assertThat(MessageEventType.RoomMessage.map()).isEqualTo(MessageLikeEventType.RoomMessage)
        assertThat(MessageEventType.RoomRedaction.map()).isEqualTo(MessageLikeEventType.RoomRedaction)
        assertThat(MessageEventType.Sticker.map()).isEqualTo(MessageLikeEventType.Sticker)
        assertThat(MessageEventType.PollEnd.map()).isEqualTo(MessageLikeEventType.PollEnd)
        assertThat(MessageEventType.PollResponse.map()).isEqualTo(MessageLikeEventType.PollResponse)
        assertThat(MessageEventType.PollStart.map()).isEqualTo(MessageLikeEventType.PollStart)
        assertThat(MessageEventType.UnstablePollEnd.map()).isEqualTo(MessageLikeEventType.UnstablePollEnd)
        assertThat(MessageEventType.UnstablePollResponse.map()).isEqualTo(MessageLikeEventType.UnstablePollResponse)
        assertThat(MessageEventType.UnstablePollStart.map()).isEqualTo(MessageLikeEventType.UnstablePollStart)
    }
}
