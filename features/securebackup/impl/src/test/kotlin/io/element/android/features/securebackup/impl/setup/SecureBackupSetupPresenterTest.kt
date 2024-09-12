/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.setup

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.libraries.matrix.api.encryption.EnableRecoveryProgress
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.test.A_RECOVERY_KEY
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SecureBackupSetupPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createSecureBackupSetupPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isChangeRecoveryKeyUserStory).isFalse()
            assertThat(initialState.setupState).isEqualTo(SetupState.Init)
            assertThat(initialState.showSaveConfirmationDialog).isFalse()
            assertThat(initialState.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Setup,
                    formattedRecoveryKey = null,
                    inProgress = false,
                )
            )
        }
    }

    @Test
    fun `present - create recovery key and save it`() = runTest {
        val encryptionService = FakeEncryptionService()
        val presenter = createSecureBackupSetupPresenter(
            encryptionService = encryptionService
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(SecureBackupSetupEvents.CreateRecoveryKey)
            val creatingState = awaitItem()
            assertThat(creatingState.setupState).isEqualTo(SetupState.Creating)
            assertThat(creatingState.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Setup,
                    formattedRecoveryKey = null,
                    inProgress = true,
                )
            )
            encryptionService.emitEnableRecoveryProgress(EnableRecoveryProgress.Done(A_RECOVERY_KEY))
            val createdState = awaitItem()
            assertThat(createdState.setupState).isEqualTo(SetupState.Created(A_RECOVERY_KEY))
            assertThat(createdState.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Setup,
                    formattedRecoveryKey = A_RECOVERY_KEY,
                    inProgress = false,
                )
            )
            createdState.eventSink.invoke(SecureBackupSetupEvents.RecoveryKeyHasBeenSaved)
            val createdAndSaveState = awaitItem()
            assertThat(createdAndSaveState.setupState).isInstanceOf(SetupState.CreatedAndSaved::class.java)
            createdAndSaveState.eventSink.invoke(SecureBackupSetupEvents.Done)
            val doneState = awaitItem()
            assertThat(doneState.showSaveConfirmationDialog).isTrue()
            doneState.eventSink.invoke(SecureBackupSetupEvents.DismissDialog)
            val doneStateCancelled = awaitItem()
            assertThat(doneStateCancelled.showSaveConfirmationDialog).isFalse()
        }
    }

    @Test
    fun `present - initial state change key`() = runTest {
        val presenter = createSecureBackupSetupPresenter(
            isChangeRecoveryKeyUserStory = true,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isChangeRecoveryKeyUserStory).isTrue()
            assertThat(initialState.setupState).isEqualTo(SetupState.Init)
            assertThat(initialState.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Change,
                    formattedRecoveryKey = null,
                    inProgress = false,
                )
            )
        }
    }

    @Test
    fun `present - change recovery key and save it`() = runTest {
        val encryptionService = FakeEncryptionService()
        val presenter = createSecureBackupSetupPresenter(
            isChangeRecoveryKeyUserStory = true,
            encryptionService = encryptionService
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(SecureBackupSetupEvents.CreateRecoveryKey)
            val creatingState = awaitItem()
            assertThat(creatingState.setupState).isEqualTo(SetupState.Creating)
            assertThat(creatingState.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Change,
                    formattedRecoveryKey = null,
                    inProgress = true,
                )
            )
            val createdState = awaitItem()
            assertThat(createdState.setupState).isEqualTo(SetupState.Created(FakeEncryptionService.FAKE_RECOVERY_KEY))
            assertThat(createdState.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Change,
                    formattedRecoveryKey = FakeEncryptionService.FAKE_RECOVERY_KEY,
                    inProgress = false,
                )
            )
            createdState.eventSink.invoke(SecureBackupSetupEvents.RecoveryKeyHasBeenSaved)
            val createdAndSaveState = awaitItem()
            assertThat(createdAndSaveState.setupState).isInstanceOf(SetupState.CreatedAndSaved::class.java)
            createdAndSaveState.eventSink.invoke(SecureBackupSetupEvents.Done)
            val doneState = awaitItem()
            assertThat(doneState.showSaveConfirmationDialog).isTrue()
            doneState.eventSink.invoke(SecureBackupSetupEvents.DismissDialog)
            val doneStateCancelled = awaitItem()
            assertThat(doneStateCancelled.showSaveConfirmationDialog).isFalse()
        }
    }

    private fun createSecureBackupSetupPresenter(
        isChangeRecoveryKeyUserStory: Boolean = false,
        encryptionService: EncryptionService = FakeEncryptionService(),
    ): SecureBackupSetupPresenter {
        return SecureBackupSetupPresenter(
            isChangeRecoveryKeyUserStory = isChangeRecoveryKeyUserStory,
            stateMachine = SecureBackupSetupStateMachine(),
            encryptionService = encryptionService,
        )
    }
}
