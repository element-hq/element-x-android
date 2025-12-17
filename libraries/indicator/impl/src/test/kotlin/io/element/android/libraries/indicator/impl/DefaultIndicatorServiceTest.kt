/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.indicator.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultIndicatorServiceTest {
    @Test
    fun `test - showRoomListTopBarIndicator`() = runTest {
        val encryptionService = FakeEncryptionService()
        val sessionVerificationService = FakeSessionVerificationService()
        val sut = DefaultIndicatorService(
            sessionVerificationService = sessionVerificationService,
            encryptionService = encryptionService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            sut.showRoomListTopBarIndicator().value
        }.test {
            assertThat(awaitItem()).isTrue()
            sessionVerificationService.emitNeedsSessionVerification(false)
            encryptionService.emitBackupState(BackupState.ENABLED)
            encryptionService.emitRecoveryState(RecoveryState.ENABLED)
            assertThat(awaitItem()).isFalse()
            sessionVerificationService.emitNeedsSessionVerification(true)
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `test - showSettingChatBackupIndicator is true when BackupState is UNKNOWN`() = runTest {
        val encryptionService = FakeEncryptionService()
        val sessionVerificationService = FakeSessionVerificationService()
        val sut = DefaultIndicatorService(
            sessionVerificationService = sessionVerificationService,
            encryptionService = encryptionService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            sut.showSettingChatBackupIndicator().value
        }.test {
            assertThat(awaitItem()).isTrue()
            encryptionService.emitBackupState(BackupState.ENABLED)
            encryptionService.emitRecoveryState(RecoveryState.ENABLED)
            assertThat(awaitItem()).isFalse()
            encryptionService.emitBackupState(BackupState.UNKNOWN)
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `test - showSettingChatBackupIndicator is true when recoveryState is DISABLED`() = runTest {
        val encryptionService = FakeEncryptionService()
        val sessionVerificationService = FakeSessionVerificationService()
        val sut = DefaultIndicatorService(
            sessionVerificationService = sessionVerificationService,
            encryptionService = encryptionService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            sut.showSettingChatBackupIndicator().value
        }.test {
            assertThat(awaitItem()).isTrue()
            encryptionService.emitBackupState(BackupState.ENABLED)
            encryptionService.emitRecoveryState(RecoveryState.ENABLED)
            assertThat(awaitItem()).isFalse()
            encryptionService.emitRecoveryState(RecoveryState.DISABLED)
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `test - showSettingChatBackupIndicator is true when recoveryState is INCOMPLETE`() = runTest {
        val encryptionService = FakeEncryptionService()
        val sessionVerificationService = FakeSessionVerificationService()
        val sut = DefaultIndicatorService(
            sessionVerificationService = sessionVerificationService,
            encryptionService = encryptionService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            sut.showSettingChatBackupIndicator().value
        }.test {
            assertThat(awaitItem()).isTrue()
            encryptionService.emitBackupState(BackupState.ENABLED)
            encryptionService.emitRecoveryState(RecoveryState.ENABLED)
            assertThat(awaitItem()).isFalse()
            encryptionService.emitRecoveryState(RecoveryState.INCOMPLETE)
            assertThat(awaitItem()).isTrue()
        }
    }
}
