/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
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
        assertThat(MessageLikeEventType.CALL_ANSWER.map()).isEqualTo(MessageEventType.CALL_ANSWER)
        assertThat(MessageLikeEventType.CALL_INVITE.map()).isEqualTo(MessageEventType.CALL_INVITE)
        assertThat(MessageLikeEventType.CALL_HANGUP.map()).isEqualTo(MessageEventType.CALL_HANGUP)
        assertThat(MessageLikeEventType.CALL_CANDIDATES.map()).isEqualTo(MessageEventType.CALL_CANDIDATES)
        assertThat(MessageLikeEventType.CALL_NOTIFY.map()).isEqualTo(MessageEventType.CALL_NOTIFY)
        assertThat(MessageLikeEventType.KEY_VERIFICATION_READY.map()).isEqualTo(MessageEventType.KEY_VERIFICATION_READY)
        assertThat(MessageLikeEventType.KEY_VERIFICATION_START.map()).isEqualTo(MessageEventType.KEY_VERIFICATION_START)
        assertThat(MessageLikeEventType.KEY_VERIFICATION_CANCEL.map()).isEqualTo(MessageEventType.KEY_VERIFICATION_CANCEL)
        assertThat(MessageLikeEventType.KEY_VERIFICATION_ACCEPT.map()).isEqualTo(MessageEventType.KEY_VERIFICATION_ACCEPT)
        assertThat(MessageLikeEventType.KEY_VERIFICATION_KEY.map()).isEqualTo(MessageEventType.KEY_VERIFICATION_KEY)
        assertThat(MessageLikeEventType.KEY_VERIFICATION_MAC.map()).isEqualTo(MessageEventType.KEY_VERIFICATION_MAC)
        assertThat(MessageLikeEventType.KEY_VERIFICATION_DONE.map()).isEqualTo(MessageEventType.KEY_VERIFICATION_DONE)
        assertThat(MessageLikeEventType.REACTION.map()).isEqualTo(MessageEventType.REACTION)
        assertThat(MessageLikeEventType.ROOM_ENCRYPTED.map()).isEqualTo(MessageEventType.ROOM_ENCRYPTED)
        assertThat(MessageLikeEventType.ROOM_MESSAGE.map()).isEqualTo(MessageEventType.ROOM_MESSAGE)
        assertThat(MessageLikeEventType.ROOM_REDACTION.map()).isEqualTo(MessageEventType.ROOM_REDACTION)
        assertThat(MessageLikeEventType.STICKER.map()).isEqualTo(MessageEventType.STICKER)
        assertThat(MessageLikeEventType.POLL_END.map()).isEqualTo(MessageEventType.POLL_END)
        assertThat(MessageLikeEventType.POLL_RESPONSE.map()).isEqualTo(MessageEventType.POLL_RESPONSE)
        assertThat(MessageLikeEventType.POLL_START.map()).isEqualTo(MessageEventType.POLL_START)
        assertThat(MessageLikeEventType.UNSTABLE_POLL_END.map()).isEqualTo(MessageEventType.UNSTABLE_POLL_END)
        assertThat(MessageLikeEventType.UNSTABLE_POLL_RESPONSE.map()).isEqualTo(MessageEventType.UNSTABLE_POLL_RESPONSE)
        assertThat(MessageLikeEventType.UNSTABLE_POLL_START.map()).isEqualTo(MessageEventType.UNSTABLE_POLL_START)
    }

    @Test
    fun `map Kotlin type should result to correct Rust type`() {
        assertThat(MessageEventType.CALL_ANSWER.map()).isEqualTo(MessageLikeEventType.CALL_ANSWER)
        assertThat(MessageEventType.CALL_INVITE.map()).isEqualTo(MessageLikeEventType.CALL_INVITE)
        assertThat(MessageEventType.CALL_HANGUP.map()).isEqualTo(MessageLikeEventType.CALL_HANGUP)
        assertThat(MessageEventType.CALL_CANDIDATES.map()).isEqualTo(MessageLikeEventType.CALL_CANDIDATES)
        assertThat(MessageEventType.CALL_NOTIFY.map()).isEqualTo(MessageLikeEventType.CALL_NOTIFY)
        assertThat(MessageEventType.KEY_VERIFICATION_READY.map()).isEqualTo(MessageLikeEventType.KEY_VERIFICATION_READY)
        assertThat(MessageEventType.KEY_VERIFICATION_START.map()).isEqualTo(MessageLikeEventType.KEY_VERIFICATION_START)
        assertThat(MessageEventType.KEY_VERIFICATION_CANCEL.map()).isEqualTo(MessageLikeEventType.KEY_VERIFICATION_CANCEL)
        assertThat(MessageEventType.KEY_VERIFICATION_ACCEPT.map()).isEqualTo(MessageLikeEventType.KEY_VERIFICATION_ACCEPT)
        assertThat(MessageEventType.KEY_VERIFICATION_KEY.map()).isEqualTo(MessageLikeEventType.KEY_VERIFICATION_KEY)
        assertThat(MessageEventType.KEY_VERIFICATION_MAC.map()).isEqualTo(MessageLikeEventType.KEY_VERIFICATION_MAC)
        assertThat(MessageEventType.KEY_VERIFICATION_DONE.map()).isEqualTo(MessageLikeEventType.KEY_VERIFICATION_DONE)
        assertThat(MessageEventType.REACTION.map()).isEqualTo(MessageLikeEventType.REACTION)
        assertThat(MessageEventType.ROOM_ENCRYPTED.map()).isEqualTo(MessageLikeEventType.ROOM_ENCRYPTED)
        assertThat(MessageEventType.ROOM_MESSAGE.map()).isEqualTo(MessageLikeEventType.ROOM_MESSAGE)
        assertThat(MessageEventType.ROOM_REDACTION.map()).isEqualTo(MessageLikeEventType.ROOM_REDACTION)
        assertThat(MessageEventType.STICKER.map()).isEqualTo(MessageLikeEventType.STICKER)
        assertThat(MessageEventType.POLL_END.map()).isEqualTo(MessageLikeEventType.POLL_END)
        assertThat(MessageEventType.POLL_RESPONSE.map()).isEqualTo(MessageLikeEventType.POLL_RESPONSE)
        assertThat(MessageEventType.POLL_START.map()).isEqualTo(MessageLikeEventType.POLL_START)
        assertThat(MessageEventType.UNSTABLE_POLL_END.map()).isEqualTo(MessageLikeEventType.UNSTABLE_POLL_END)
        assertThat(MessageEventType.UNSTABLE_POLL_RESPONSE.map()).isEqualTo(MessageLikeEventType.UNSTABLE_POLL_RESPONSE)
        assertThat(MessageEventType.UNSTABLE_POLL_START.map()).isEqualTo(MessageLikeEventType.UNSTABLE_POLL_START)
    }
}
