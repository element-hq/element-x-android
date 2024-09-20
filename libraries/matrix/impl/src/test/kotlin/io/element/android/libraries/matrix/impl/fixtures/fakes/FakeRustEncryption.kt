/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.Encryption
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.RecoveryStateListener
import org.matrix.rustcomponents.sdk.TaskHandle
import org.matrix.rustcomponents.sdk.VerificationStateListener

class FakeRustEncryption : Encryption(NoPointer) {
    override fun verificationStateListener(listener: VerificationStateListener): TaskHandle {
        return FakeRustTaskHandle()
    }

    override fun recoveryStateListener(listener: RecoveryStateListener): TaskHandle {
        return FakeRustTaskHandle()
    }
}
