/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.root

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ResetIdentityRootPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isResetTheOnlyOption).isFalse()
            assertThat(initialState.displayConfirmationDialog).isFalse()
        }
    }

    @Test
    fun `present - Continue event displays the confirmation dialog`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ResetIdentityRootEvent.Continue)

            assertThat(awaitItem().displayConfirmationDialog).isTrue()
        }
    }

    @Test
    fun `present - DismissDialog event hides the confirmation dialog`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ResetIdentityRootEvent.Continue)
            assertThat(awaitItem().displayConfirmationDialog).isTrue()

            initialState.eventSink(ResetIdentityRootEvent.DismissDialog)
            assertThat(awaitItem().displayConfirmationDialog).isFalse()
        }
    }

    @Test
    fun `present - without other options and with encrypted chats`() = runTest {
        val presenter = createPresenter(
            encryptionService = anEncryptionServiceWithoutOptions(),
            matrixClient = FakeMatrixClient(hasEncryptedRoomsResult = { Result.success(true) }),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val loadingState = awaitItem()
            assertThat(loadingState.isResetTheOnlyOption).isTrue()
            assertThat(loadingState.hasEncryptedRooms.isLoading()).isTrue()
            assertThat(awaitItem().hasEncryptedRooms).isEqualTo(AsyncData.Success(true))
        }
    }

    @Test
    fun `present - without other options and without encrypted chats`() = runTest {
        val presenter = createPresenter(
            encryptionService = anEncryptionServiceWithoutOptions(),
            matrixClient = FakeMatrixClient(hasEncryptedRoomsResult = { Result.success(false) }),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().hasEncryptedRooms.isLoading()).isTrue()
            assertThat(awaitItem().hasEncryptedRooms).isEqualTo(AsyncData.Success(false))
        }
    }

    @Test
    fun `present - without other options, failing to know about the chats is treated as having encrypted chats`() = runTest {
        val presenter = createPresenter(
            encryptionService = anEncryptionServiceWithoutOptions(),
            matrixClient = FakeMatrixClient(hasEncryptedRoomsResult = { Result.failure(IllegalStateException("Failed")) }),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().hasEncryptedRooms.isLoading()).isTrue()
            assertThat(awaitItem().hasEncryptedRooms).isEqualTo(AsyncData.Success(true))
        }
    }

    @Test
    fun `present - another device becoming available means reset is no longer the only option`() = runTest {
        val encryptionService = anEncryptionServiceWithoutOptions()
        val presenter = createPresenter(encryptionService = encryptionService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().isResetTheOnlyOption).isTrue()
            assertThat(awaitItem().hasEncryptedRooms).isEqualTo(AsyncData.Success(true))

            encryptionService.emitHasDevicesToVerifyAgainst(AsyncData.Success(true))
            assertThat(awaitItem().isResetTheOnlyOption).isFalse()
        }
    }

    @Test
    fun `present - a recovery key becoming available means reset is no longer the only option`() = runTest {
        val encryptionService = anEncryptionServiceWithoutOptions()
        val presenter = createPresenter(encryptionService = encryptionService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().isResetTheOnlyOption).isTrue()
            assertThat(awaitItem().hasEncryptedRooms).isEqualTo(AsyncData.Success(true))

            encryptionService.emitRecoveryState(RecoveryState.INCOMPLETE)
            assertThat(awaitItem().isResetTheOnlyOption).isFalse()
        }
    }

    @Test
    fun `present - a verified session is never offered reset as the only option`() = runTest {
        val presenter = createPresenter(
            encryptionService = anEncryptionServiceWithoutOptions(),
            sessionVerificationService = FakeSessionVerificationService(initialSessionVerifiedStatus = SessionVerifiedStatus.Verified),
            matrixClient = FakeMatrixClient(hasEncryptedRoomsResult = { error("Should not be called") }),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().isResetTheOnlyOption).isFalse()
        }
    }

    private fun anEncryptionServiceWithoutOptions() = FakeEncryptionService().apply {
        emitHasDevicesToVerifyAgainst(AsyncData.Success(false))
        recoveryStateStateFlow.value = RecoveryState.DISABLED
    }

    private fun createPresenter(
        matrixClient: MatrixClient = FakeMatrixClient(),
        encryptionService: FakeEncryptionService = FakeEncryptionService(),
        sessionVerificationService: FakeSessionVerificationService = FakeSessionVerificationService(
            initialSessionVerifiedStatus = SessionVerifiedStatus.NotVerified,
        ),
    ) = ResetIdentityRootPresenter(
        matrixClient = matrixClient,
        encryptionService = encryptionService,
        sessionVerificationService = sessionVerificationService,
    )
}
