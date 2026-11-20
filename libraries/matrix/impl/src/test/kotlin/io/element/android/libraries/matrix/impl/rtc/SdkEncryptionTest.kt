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
import uniffi.matrix_sdk_ui.TimelineEventShieldStateCode

class SdkEncryptionTest {
    @Test
    fun `no encryption info is cleartext, with nothing said about a sender`() {
        val info: EventEncryptionInfo? = null

        assertThat(info.toFfi()).isEqualTo(FfiEventEncryption(encrypted = false, senderDeviceId = null, senderConfirmed = null))
    }

    @Test
    fun `an encrypted event is confirmed under a none or grey shield`() {
        listOf(
            ShieldState.None,
            ShieldState.Grey(TimelineEventShieldStateCode.UNKNOWN_DEVICE),
            ShieldState.Grey(TimelineEventShieldStateCode.AUTHENTICITY_NOT_GUARANTEED),
        ).forEach { shield ->
            assertThat(anEncryptionInfo(ALICE, shield).toFfi())
                .isEqualTo(FfiEventEncryption(encrypted = true, senderDeviceId = A_SENDER_DEVICE, senderConfirmed = true))
        }
    }

    @Test
    fun `an encrypted event under a red shield is not confirmed`() {
        listOf(
            ShieldState.Red(TimelineEventShieldStateCode.UNSIGNED_DEVICE),
            ShieldState.Red(TimelineEventShieldStateCode.VERIFICATION_VIOLATION),
            ShieldState.Red(TimelineEventShieldStateCode.MISMATCHED_SENDER),
        ).forEach { shield ->
            assertThat(anEncryptionInfo(ALICE, shield).toFfi().senderConfirmed).isFalse()
        }
    }

    private companion object {
        const val ALICE = "@alice:example.org"
    }
}
