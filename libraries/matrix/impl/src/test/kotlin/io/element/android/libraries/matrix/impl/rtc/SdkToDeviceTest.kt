/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.rtc

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.matrix.rtc.FfiToDeviceRecipient
import org.matrix.rustcomponents.sdk.ShieldState
import org.matrix.rustcomponents.sdk.ToDeviceMessage

class SdkToDeviceTest {
    @Test
    fun `an encrypted message comes from its attested sender, not the claimed one`() {
        val message = ToDeviceMessage(
            eventType = A_KEY_TYPE,
            senderId = "@claimed:example.org",
            content = A_CONTENT,
            encryptionInfo = anEncryptionInfo(ALICE, ShieldState.None),
        ).toFfi()

        assertThat(message.sender).isEqualTo(ALICE)
        assertThat(message.eventType).isEqualTo(A_KEY_TYPE)
        assertThat(message.contentJson).isEqualTo(A_CONTENT)
        assertThat(message.encryption.encrypted).isTrue()
        assertThat(message.encryption.senderDeviceId).isEqualTo(A_SENDER_DEVICE)
        assertThat(message.encryption.senderConfirmed).isTrue()
    }

    @Test
    fun `a message in the clear says so and keeps its claimed sender`() {
        val message = ToDeviceMessage(eventType = A_KEY_TYPE, senderId = ALICE, content = A_CONTENT, encryptionInfo = null).toFfi()

        assertThat(message.sender).isEqualTo(ALICE)
        assertThat(message.encryption.encrypted).isFalse()
    }

    @Test
    fun `every recipient gets a verdict, failed only where the SDK could not serve it`() {
        val recipients = listOf(
            FfiToDeviceRecipient(ALICE, "A1"),
            FfiToDeviceRecipient(ALICE, "A2"),
            FfiToDeviceRecipient(BOB, "B1"),
        )

        val deliveries = recipients.toDeliveries(mapOf(ALICE to listOf("A2")))

        assertThat(deliveries.map { Triple(it.userId, it.deviceId, it.error != null) }).containsExactly(
            Triple(ALICE, "A1", false),
            Triple(ALICE, "A2", true),
            Triple(BOB, "B1", false),
        ).inOrder()
    }

    private companion object {
        const val A_KEY_TYPE = "io.element.call.encryption_keys"
        const val A_CONTENT = """{"keys":[]}"""
        const val ALICE = "@alice:example.org"
        const val BOB = "@bob:example.org"
    }
}
