/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.rtc

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.matrix.rtc.FfiEventEncryption
import org.matrix.rustcomponents.sdk.EventEncryptionInfo
import org.matrix.rustcomponents.sdk.ShieldState
import org.matrix.rustcomponents.sdk.StickyEvent
import org.matrix.rustcomponents.sdk.StickyEventRemoval
import org.matrix.rustcomponents.sdk.StickyEventsUpdate
import org.matrix.rustcomponents.sdk.StickyKey
import uniffi.matrix_sdk_base.RemovalReason

class StickyEventSetTest {
    @Test
    fun `a reset is the whole set, even an empty one`() {
        val set = StickyEventSet()

        assertThat(set.apply(StickyEventsUpdate.Reset(emptyList()))).isEmpty()
        val events = set.apply(StickyEventsUpdate.Reset(listOf(aStickyEvent(ALICE, "\$a"), aStickyEvent(BOB, "\$b"))))

        assertThat(events.map { it.eventId }).containsExactly("\$a", "\$b").inOrder()
    }

    @Test
    fun `changes add, replace and remove by key`() {
        val set = StickyEventSet()
        set.apply(StickyEventsUpdate.Reset(listOf(aStickyEvent(ALICE, "\$a1"), aStickyEvent(BOB, "\$b"))))

        val events = set.apply(
            StickyEventsUpdate.Changes(
                added = listOf(aStickyEvent(CAROL, "\$c")),
                updated = listOf(aStickyEvent(ALICE, "\$a2")),
                removed = listOf(StickyEventRemoval(aKey(BOB), RemovalReason.EXPIRED)),
            )
        )

        assertThat(events.map { it.eventId }).containsExactly("\$a2", "\$c")
    }

    @Test
    fun `a later reset drops what it does not list`() {
        val set = StickyEventSet()
        set.apply(StickyEventsUpdate.Reset(listOf(aStickyEvent(ALICE, "\$a"), aStickyEvent(BOB, "\$b"))))

        val events = set.apply(StickyEventsUpdate.Reset(listOf(aStickyEvent(BOB, "\$b"))))

        assertThat(events.map { it.eventId }).containsExactly("\$b")
    }

    @Test
    fun `a malformed event is left out, and drops the one it replaced`() {
        val set = StickyEventSet()
        set.apply(StickyEventsUpdate.Reset(listOf(aStickyEvent(ALICE, "\$a"))))

        val events = set.apply(
            StickyEventsUpdate.Changes(added = emptyList(), updated = listOf(aStickyEvent(ALICE, "\$a2", eventJson = "not json")), removed = emptyList())
        )

        assertThat(events).isEmpty()
    }

    @Test
    fun `the event carries its wire type, content and timestamp`() {
        val event = aStickyEvent(ALICE, "\$a").toFfiEventIn()!!

        assertThat(event.sender).isEqualTo(ALICE)
        assertThat(event.eventType).isEqualTo(A_DECLINE_TYPE)
        assertThat(event.stateKey).isNull()
        assertThat(event.originServerTs).isEqualTo(1_000uL)
        assertThat(event.contentJson).isEqualTo("""{"m.relates_to":{"rel_type":"m.reference","event_id":"${'$'}ring"}}""")
        assertThat(event.encryption.encrypted).isFalse()
    }

    @Test
    fun `an encrypted event carries its sending device and whether it is cross-signed`() {
        val event = aStickyEvent(ALICE, "\$a", encryption = anEncryptionInfo(ALICE, ShieldState.None)).toFfiEventIn()!!

        assertThat(event.encryption).isEqualTo(FfiEventEncryption(encrypted = true, senderDeviceId = A_SENDER_DEVICE, senderConfirmed = true))
    }

    private fun aKey(sender: String) = StickyKey(sender = sender, eventType = A_DECLINE_TYPE, stickyKey = "key-$sender")

    private fun aStickyEvent(
        sender: String,
        eventId: String,
        eventJson: String = """{"type":"$A_DECLINE_TYPE","sender":"$sender","event_id":"$eventId","origin_server_ts":1000,""" +
            """"content":{"m.relates_to":{"rel_type":"m.reference","event_id":"${'$'}ring"}}}""",
        encryption: EventEncryptionInfo? = null,
    ) = StickyEvent(key = aKey(sender), eventId = eventId, expiresAtMs = 3_600_000uL, eventJson = eventJson, encryptionInfo = encryption)

    private companion object {
        const val ALICE = "@alice:example.org"
        const val BOB = "@bob:example.org"
        const val CAROL = "@carol:example.org"
        const val A_DECLINE_TYPE = "org.matrix.msc4310.rtc.decline"
    }
}
