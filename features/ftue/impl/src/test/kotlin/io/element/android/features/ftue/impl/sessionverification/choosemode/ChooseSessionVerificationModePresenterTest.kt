/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.sessionverification.choosemode

import com.google.common.truth.Truth.assertThat
import io.element.android.features.logout.api.direct.DirectLogoutEvent
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.features.logout.api.direct.aDirectLogoutState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ChooseSessionVerificationModePresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        presenter.test {
            awaitItem().run {
                assertThat(buttonsState.isLoading()).isTrue()
                assertThat(directLogoutState.logoutAction.isUninitialized()).isTrue()
            }
        }
    }

    @Test
    fun `present - current user is exposed`() = runTest {
        val presenter = createPresenter(
            matrixClient = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                userDisplayName = A_USER_NAME,
                userAvatarUrl = AN_AVATAR_URL,
            ),
        )
        presenter.test {
            assertThat(awaitItem().currentUser).isEqualTo(
                MatrixUser(
                    userId = A_SESSION_ID,
                    displayName = A_USER_NAME,
                    avatarUrl = AN_AVATAR_URL,
                )
            )
        }
    }

    @Test
    fun `present - state is relayed from EncryptionService, order 1`() = runTest {
        val encryptionService = FakeEncryptionService()
        val presenter = createPresenter(encryptionService = encryptionService)
        presenter.test {
            assertThat(awaitItem().buttonsState.isLoading()).isTrue()
            // Has device to verify against
            encryptionService.emitHasDevicesToVerifyAgainst(AsyncData.Success(false))
            // Can enter recovery key
            encryptionService.emitRecoveryState(RecoveryState.DISABLED)
            assertThat(awaitItem().buttonsState.dataOrNull()).isEqualTo(
                ChooseSelfVerificationModeState.ButtonsState(
                    canUseAnotherDevice = false,
                    canUseRecoveryKey = false,
                )
            )
        }
    }

    @Test
    fun `present - state is relayed from EncryptionService, order 2`() = runTest {
        val encryptionService = FakeEncryptionService()
        val presenter = createPresenter(encryptionService = encryptionService)
        presenter.test {
            assertThat(awaitItem().buttonsState.isLoading()).isTrue()
            // Can enter recovery key
            encryptionService.emitRecoveryState(RecoveryState.DISABLED)
            // Has device to verify against
            encryptionService.emitHasDevicesToVerifyAgainst(AsyncData.Success(false))
            assertThat(awaitItem().buttonsState.dataOrNull()).isEqualTo(
                ChooseSelfVerificationModeState.ButtonsState(
                    canUseAnotherDevice = false,
                    canUseRecoveryKey = false,
                )
            )
        }
    }

    @Test
    fun `present - can use another device`() = runTest {
        val encryptionService = FakeEncryptionService()
        val presenter = createPresenter(encryptionService = encryptionService)
        presenter.test {
            assertThat(awaitItem().buttonsState.isLoading()).isTrue()
            // Can enter recovery key
            encryptionService.emitRecoveryState(RecoveryState.DISABLED)
            // Has device to verify against
            encryptionService.emitHasDevicesToVerifyAgainst(AsyncData.Success(true))
            assertThat(awaitItem().buttonsState.dataOrNull()).isEqualTo(
                ChooseSelfVerificationModeState.ButtonsState(
                    canUseAnotherDevice = true,
                    canUseRecoveryKey = false,
                )
            )
        }
    }

    @Test
    fun `present - can enter recovery key`() = runTest {
        val encryptionService = FakeEncryptionService()
        val presenter = createPresenter(encryptionService = encryptionService)
        presenter.test {
            assertThat(awaitItem().buttonsState.isLoading()).isTrue()
            // Can enter recovery key
            encryptionService.emitRecoveryState(RecoveryState.INCOMPLETE)
            // Has device to verify against
            encryptionService.emitHasDevicesToVerifyAgainst(AsyncData.Success(false))
            assertThat(awaitItem().buttonsState.dataOrNull()).isEqualTo(
                ChooseSelfVerificationModeState.ButtonsState(
                    canUseAnotherDevice = false,
                    canUseRecoveryKey = true,
                )
            )
        }
    }

    @Test
    fun `sing out action triggers a direct logout`() = runTest {
        val logoutEventRecorder = lambdaRecorder<DirectLogoutEvent, Unit> {}
        val logoutPresenter = Presenter<DirectLogoutState> {
            aDirectLogoutState(eventSink = logoutEventRecorder)
        }
        val presenter = createPresenter(directLogoutPresenter = logoutPresenter)
        presenter.test {
            val initial = awaitItem()
            initial.eventSink(ChooseSelfVerificationModeEvent.SignOut)
            logoutEventRecorder.assertions().isCalledOnce()
                .with(value(DirectLogoutEvent.Logout(ignoreSdkError = false)))
        }
    }

    private fun createPresenter(
        matrixClient: MatrixClient = FakeMatrixClient(),
        encryptionService: FakeEncryptionService = FakeEncryptionService(),
        directLogoutPresenter: Presenter<DirectLogoutState> = Presenter<DirectLogoutState> { aDirectLogoutState() }
    ) = ChooseSelfVerificationModePresenter(
        matrixClient = matrixClient,
        encryptionService = encryptionService,
        directLogoutPresenter = directLogoutPresenter,
    )
}
