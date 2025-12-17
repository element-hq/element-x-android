/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.tests.testutils.simulateLongTask
import org.matrix.rustcomponents.sdk.BackupState
import org.matrix.rustcomponents.sdk.BackupStateListener
import org.matrix.rustcomponents.sdk.Encryption
import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.RecoveryState
import org.matrix.rustcomponents.sdk.RecoveryStateListener
import org.matrix.rustcomponents.sdk.TaskHandle
import org.matrix.rustcomponents.sdk.VerificationStateListener

class FakeFfiEncryption : Encryption(NoHandle) {
    override fun verificationStateListener(listener: VerificationStateListener): TaskHandle {
        return FakeFfiTaskHandle()
    }

    override fun recoveryStateListener(listener: RecoveryStateListener): TaskHandle {
        return FakeFfiTaskHandle()
    }

    override suspend fun waitForE2eeInitializationTasks() = simulateLongTask {}

    override suspend fun isLastDevice(): Boolean {
        return false
    }

    override suspend fun hasDevicesToVerifyAgainst(): Boolean {
        return true
    }

    override fun backupState(): BackupState {
        return BackupState.ENABLED
    }

    override fun recoveryState(): RecoveryState {
        return RecoveryState.ENABLED
    }

    override fun backupStateListener(listener: BackupStateListener): TaskHandle {
        return FakeFfiTaskHandle()
    }
}
