/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.scan

import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.localnetwork.LocalNetworkPermissionGate
import io.element.android.features.login.impl.qrcode.FakeQrCodeLoginManager
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.api.auth.qrlogin.QrLoginException
import io.element.android.libraries.matrix.test.auth.qrlogin.FakeMatrixQrCodeLoginData
import io.element.android.libraries.matrix.test.auth.qrlogin.FakeMatrixQrCodeLoginDataFactory
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.api.aPermissionsState
import io.element.android.libraries.permissions.api.localnetwork.LocalNetworkPermissionAdvisor
import io.element.android.libraries.permissions.api.localnetwork.LocalNetworkPermissionDialog
import io.element.android.libraries.permissions.test.FakeLocalNetworkPermissionAdvisor
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class QrCodeScanPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createQrCodeScanPresenter()
        presenter.test {
            awaitItem().run {
                assertThat(isScanning).isTrue()
                assertThat(authenticationAction.isUninitialized()).isTrue()
                assertThat(localNetworkPermissionDialog).isEqualTo(LocalNetworkPermissionDialog.None)
            }
        }
    }

    @Test
    fun `present - scanned QR code successfully when no local network permission is needed`() = runTest {
        val qrCodeLoginDataFactory = FakeMatrixQrCodeLoginDataFactory(
            parseQrCodeLoginDataResult = {
                Result.success(
                    FakeMatrixQrCodeLoginData(
                        serverNameResult = { "example.com" }
                    )
                )
            }
        )
        val presenter = createQrCodeScanPresenter(
            qrCodeLoginDataFactory = qrCodeLoginDataFactory,
            // Advisor does not require the local network permission for this homeserver.
            localNetworkPermissionGate = createLocalNetworkPermissionGate(shouldPrompt = false),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(QrCodeScanEvent.QrCodeScanned(byteArrayOf()))
            assertThat(awaitItem().isScanning).isFalse()
            assertThat(awaitItem().authenticationAction.isLoading()).isTrue()
            // The gate does not require the permission, so it flips canProceed to true and no dialog is shown.
            val readyState = consumeItemsUntilPredicate {
                it.authenticationAction.dataOrNull()?.canProceed == true
            }.last()
            assertThat(readyState.authenticationAction.isSuccess()).isTrue()
            assertThat(readyState.localNetworkPermissionDialog).isEqualTo(LocalNetworkPermissionDialog.None)
        }
    }

    @Test
    fun `present - scanned QR code with local homeserver shows the permission dialog then proceeds once granted`() = runTest {
        val qrCodeLoginDataFactory = FakeMatrixQrCodeLoginDataFactory(
            parseQrCodeLoginDataResult = {
                Result.success(
                    FakeMatrixQrCodeLoginData(
                        serverNameResult = { "localhost" }
                    )
                )
            }
        )
        val permissionsPresenter = FakePermissionsPresenter(
            initialState = aPermissionsState(showDialog = false)
        )
        val presenter = createQrCodeScanPresenter(
            qrCodeLoginDataFactory = qrCodeLoginDataFactory,
            // Advisor requires the local network permission for this homeserver.
            localNetworkPermissionGate = createLocalNetworkPermissionGate(
                shouldPrompt = true,
                permissionsPresenter = permissionsPresenter,
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(QrCodeScanEvent.QrCodeScanned(byteArrayOf()))
            // The gate requires the permission, so the rationale dialog is displayed.
            val dialogState = consumeItemsUntilPredicate {
                it.localNetworkPermissionDialog == LocalNetworkPermissionDialog.Rationale
            }.last()
            // The data is still not allowed to proceed while the dialog is displayed.
            assertThat(dialogState.authenticationAction.dataOrNull()?.canProceed).isFalse()

            // The user grants the permission.
            permissionsPresenter.setPermissionGranted()

            val readyState = consumeItemsUntilPredicate {
                it.authenticationAction.dataOrNull()?.canProceed == true
            }.last()
            assertThat(readyState.authenticationAction.isSuccess()).isTrue()
            assertThat(readyState.localNetworkPermissionDialog).isEqualTo(LocalNetworkPermissionDialog.None)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - scanned QR code with local homeserver fails when the permission is denied and the dialog is dismissed`() = runTest {
        val qrCodeLoginDataFactory = FakeMatrixQrCodeLoginDataFactory(
            parseQrCodeLoginDataResult = {
                Result.success(
                    FakeMatrixQrCodeLoginData(
                        serverNameResult = { "localhost" }
                    )
                )
            }
        )
        val permissionsPresenter = FakePermissionsPresenter(
            initialState = aPermissionsState(showDialog = false)
        )
        val presenter = createQrCodeScanPresenter(
            qrCodeLoginDataFactory = qrCodeLoginDataFactory,
            localNetworkPermissionGate = createLocalNetworkPermissionGate(
                shouldPrompt = true,
                permissionsPresenter = permissionsPresenter,
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(QrCodeScanEvent.QrCodeScanned(byteArrayOf()))
            // The gate requires the permission, so the rationale dialog is displayed.
            val rationaleState = consumeItemsUntilPredicate {
                it.localNetworkPermissionDialog == LocalNetworkPermissionDialog.Rationale
            }.last()

            // The user chooses to grant the permission, triggering the system request, but then denies it.
            rationaleState.eventSink(QrCodeScanEvent.RequestLocalNetworkAccessPermission)
            permissionsPresenter.setPermissionDenied()

            // The permission is now permanently denied, so the settings dialog is displayed.
            val settingsState = consumeItemsUntilPredicate {
                it.localNetworkPermissionDialog == LocalNetworkPermissionDialog.Settings
            }.last()

            // The user dismisses the dialog instead of going to the settings.
            settingsState.eventSink(QrCodeScanEvent.DismissLocalNetworkAccessPermissionDialog)

            val failureState = consumeItemsUntilPredicate {
                it.authenticationAction.errorOrNull() is CannotAccessLocalHomeserverException
            }.last()
            assertThat(failureState.authenticationAction.isFailure()).isTrue()
            assertThat(failureState.localNetworkPermissionDialog).isEqualTo(LocalNetworkPermissionDialog.None)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - does not fail immediately when the local network permission was previously denied`() = runTest {
        val permissionsPresenter = FakePermissionsPresenter(
            // The permission was permanently denied in a previous session.
            initialState = aPermissionsState(showDialog = false).copy(
                permissionAlreadyAsked = true,
                permissionAlreadyDenied = true,
            )
        )
        val presenter = createQrCodeScanPresenter(
            localNetworkPermissionGate = createLocalNetworkPermissionGate(
                shouldPrompt = true,
                permissionsPresenter = permissionsPresenter,
            ),
        )
        presenter.test {
            // The screen must remain usable: no failure is emitted until the user actually scans a code.
            val state = awaitLastSequentialItem()
            assertThat(state.isScanning).isTrue()
            assertThat(state.authenticationAction.isUninitialized()).isTrue()
            assertThat(state.localNetworkPermissionDialog).isEqualTo(LocalNetworkPermissionDialog.None)
        }
    }

    @Test
    fun `present - scanned QR code failed and can be retried`() = runTest {
        val qrCodeLoginDataFactory = FakeMatrixQrCodeLoginDataFactory(
            parseQrCodeLoginDataResult = { Result.failure(Exception("Failed to parse QR code")) }
        )
        val presenter = createQrCodeScanPresenter(qrCodeLoginDataFactory = qrCodeLoginDataFactory)
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(QrCodeScanEvent.QrCodeScanned(byteArrayOf()))
            assertThat(awaitItem().isScanning).isFalse()
            assertThat(awaitItem().authenticationAction.isLoading()).isTrue()

            val errorState = awaitItem()
            assertThat(errorState.authenticationAction.isFailure()).isTrue()

            errorState.eventSink(QrCodeScanEvent.TryAgain)
            assertThat(awaitItem().isScanning).isTrue()
            assertThat(awaitItem().authenticationAction.isUninitialized()).isTrue()
        }
    }

    @Test
    fun `present - login failed with so we display the error and recover from it`() = runTest {
        val qrCodeLoginDataFactory = FakeMatrixQrCodeLoginDataFactory()
        val qrCodeLoginManager = FakeQrCodeLoginManager()
        val resetAction = lambdaRecorder<Unit> {
            qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Uninitialized
        }
        qrCodeLoginManager.resetAction = resetAction
        val presenter = createQrCodeScanPresenter(qrCodeLoginDataFactory = qrCodeLoginDataFactory, qrCodeLoginManager = qrCodeLoginManager)
        presenter.test {
            // Skip initial item
            skipItems(1)

            qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Failed(QrLoginException.OtherDeviceNotSignedIn)

            val errorState = awaitItem()
            // The state for this screen is failure
            assertThat(errorState.authenticationAction.isFailure()).isTrue()
            // However, the QrCodeLoginManager is reset
            resetAction.assertions().isCalledOnce()
            assertThat(qrCodeLoginManager.currentLoginStep.value).isEqualTo(QrCodeLoginStep.Uninitialized)
        }
    }

    private fun createLocalNetworkPermissionGate(
        shouldPrompt: Boolean = false,
        advisor: LocalNetworkPermissionAdvisor = FakeLocalNetworkPermissionAdvisor(shouldPrompt = shouldPrompt),
        permissionsPresenter: PermissionsPresenter = FakePermissionsPresenter(),
    ) = LocalNetworkPermissionGate(
        advisor = advisor,
        permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionsPresenter),
    )

    private fun TestScope.createQrCodeScanPresenter(
        qrCodeLoginDataFactory: FakeMatrixQrCodeLoginDataFactory = FakeMatrixQrCodeLoginDataFactory(),
        coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
        qrCodeLoginManager: FakeQrCodeLoginManager = FakeQrCodeLoginManager(),
        localNetworkPermissionGate: LocalNetworkPermissionGate = createLocalNetworkPermissionGate(),
    ) = QrCodeScanPresenter(
        qrCodeLoginDataFactory = qrCodeLoginDataFactory,
        qrCodeLoginManager = qrCodeLoginManager,
        coroutineDispatchers = coroutineDispatchers,
        localNetworkPermissionGate = localNetworkPermissionGate,
    )
}
