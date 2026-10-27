/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.rtc

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.matrix.rtc.FfiBackendException
import org.matrix.rustcomponents.sdk.ClientException
import org.matrix.rustcomponents.sdk.ErrorKind
import org.matrix.rustcomponents.sdk.EventEncryptionInfo
import org.matrix.rustcomponents.sdk.RoomMessageLikeEvent
import org.matrix.rustcomponents.sdk.RoomStateEvent
import org.matrix.rustcomponents.sdk.ShieldState
import uniffi.ruma_events.MessageLikeEventType
import uniffi.ruma_events.StateEventType

class SdkRoomEventsTest {
    @Test
    fun `a state event is reported under the type subscribed to, in the clear`() {
        val event = aStateEvent(eventId = "\$member").toFfiEventIn(A_MEMBER_TYPE)!!

        assertThat(event.eventId).isEqualTo("\$member")
        assertThat(event.eventType).isEqualTo(A_MEMBER_TYPE)
        assertThat(event.stateKey).isEqualTo("_${ALICE}_DEVICE")
        assertThat(event.sender).isEqualTo(ALICE)
        assertThat(event.originServerTs).isEqualTo(1_000uL)
        assertThat(event.contentJson).isEqualTo(A_CONTENT)
        assertThat(event.encryption.encrypted).isFalse()
    }

    @Test
    fun `stripped state, which has no event id, is left out`() {
        assertThat(aStateEvent(eventId = null).toFfiEventIn(A_MEMBER_TYPE)).isNull()
    }

    @Test
    fun `a message-like event carries its encryption`() {
        val event = aMessageLikeEvent(A_CONTENT, anEncryptionInfo(ALICE, ShieldState.None)).toFfiEventIn(A_REACTION_TYPE)

        assertThat(event.eventType).isEqualTo(A_REACTION_TYPE)
        assertThat(event.stateKey).isNull()
        assertThat(event.encryption.encrypted).isTrue()
        assertThat(event.encryption.senderDeviceId).isEqualTo(A_SENDER_DEVICE)
    }

    @Test
    fun `a redaction names the event it redacts from its content`() {
        val redaction = aMessageLikeEvent("""{"redacts":"${'$'}hand"}""").toFfiEventIn(REDACTION_EVENT_TYPE)

        assertThat(redaction.redactedEventId()).isEqualTo("\$hand")
        assertThat(aMessageLikeEvent("{}").toFfiEventIn(REDACTION_EVENT_TYPE).redactedEventId()).isNull()
    }

    @Test
    fun `a Matrix error reaches the core with its errcode`() {
        val failure = ClientException.MatrixApi(ErrorKind.Forbidden, "M_FORBIDDEN", "Not allowed", null).toFfiBackendException() as FfiBackendException.Failed

        assertThat(failure.errcode).isEqualTo("M_FORBIDDEN")
        assertThat(failure.status).isNull()
        assertThat(failure.reason).isEqualTo("Not allowed")
        assertThat(failure.retryAfterMs).isNull()
        assertThat(failure.permanent).isFalse()
    }

    @Test
    fun `a rate limit reaches the core with its retry-after`() {
        val failure = ClientException.MatrixApi(ErrorKind.LimitExceeded(7_000uL), "M_LIMIT_EXCEEDED", "Too many requests", null)
            .toFfiBackendException() as FfiBackendException.Failed

        assertThat(failure.errcode).isEqualTo("M_LIMIT_EXCEEDED")
        assertThat(failure.retryAfterMs).isEqualTo(7_000uL)
    }

    @Test
    fun `any other failure reaches the core as its message`() {
        val failure = IllegalStateException("boom").toFfiBackendException() as FfiBackendException.Failed

        assertThat(failure.errcode).isNull()
        assertThat(failure.reason).isEqualTo("boom")
    }

    private fun aStateEvent(eventId: String?) = RoomStateEvent(
        eventType = StateEventType.CallMember,
        stateKey = "_${ALICE}_DEVICE",
        sender = ALICE,
        contentJson = A_CONTENT,
        eventId = eventId,
        timestamp = 1_000uL,
    )

    private fun aMessageLikeEvent(content: String, encryption: EventEncryptionInfo? = null) = RoomMessageLikeEvent(
        eventType = MessageLikeEventType.Reaction,
        eventId = "\$event",
        sender = ALICE,
        timestamp = 2_000uL,
        contentJson = content,
        encryptionInfo = encryption,
    )

    private companion object {
        const val ALICE = "@alice:example.org"
        const val A_MEMBER_TYPE = "org.matrix.msc3401.call.member"
        const val A_REACTION_TYPE = "m.reaction"
        const val A_CONTENT = """{"application":"m.call"}"""
    }
}
