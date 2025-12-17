/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.qrcode

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.AncestryInfo
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.utils.customisations.NodeCustomisationDirectoryImpl
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.di.FakeQrCodeLoginGraph
import io.element.android.features.login.impl.screens.qrcode.confirmation.QrCodeConfirmationStep
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.api.auth.qrlogin.QrLoginException
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.libraries.matrix.test.auth.qrlogin.FakeMatrixQrCodeLoginData
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrCodeLoginFlowNodeTest {
    @Test
    fun `backstack changes when confirmation steps are received`() = runTest {
        val qrCodeLoginManager = FakeQrCodeLoginManager()
        val flowNode = createLoginFlowNode(qrCodeLoginManager = qrCodeLoginManager)
        flowNode.observeLoginStep()
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.Initial)

        qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.EstablishingSecureChannel("12")
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.QrCodeConfirmation(QrCodeConfirmationStep.DisplayCheckCode("12")))

        qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.WaitingForToken("123456")
        assertThat(flowNode.currentNavTarget())
            .isEqualTo(QrCodeLoginFlowNode.NavTarget.QrCodeConfirmation(QrCodeConfirmationStep.DisplayVerificationCode("123456")))
    }

    @Test
    fun `backstack changes when failure step is received`() = runTest {
        val qrCodeLoginManager = FakeQrCodeLoginManager()
        val flowNode = createLoginFlowNode(qrCodeLoginManager = qrCodeLoginManager)
        flowNode.observeLoginStep()
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.Initial)

        // Only case when this doesn't happen, since it's handled by the already displayed UI
        qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Failed(QrLoginException.OtherDeviceNotSignedIn)
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.Initial)

        qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Failed(QrLoginException.Expired)
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.Error(QrCodeErrorScreenType.Expired))

        qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Failed(QrLoginException.Declined)
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.Error(QrCodeErrorScreenType.Declined))

        qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Failed(QrLoginException.Cancelled)
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.Error(QrCodeErrorScreenType.Cancelled))

        qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Failed(QrLoginException.SlidingSyncNotAvailable)
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.Error(QrCodeErrorScreenType.SlidingSyncNotAvailable))

        qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Failed(QrLoginException.LinkingNotSupported)
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.Error(QrCodeErrorScreenType.ProtocolNotSupported))

        qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Failed(QrLoginException.ConnectionInsecure)
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.Error(QrCodeErrorScreenType.InsecureChannelDetected))

        qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Failed(QrLoginException.OidcMetadataInvalid)
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.Error(QrCodeErrorScreenType.UnknownError))

        qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Failed(QrLoginException.Unknown)
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.Error(QrCodeErrorScreenType.UnknownError))
    }

    @Test
    fun `backstack doesn't change when other steps are received`() = runTest {
        val qrCodeLoginManager = FakeQrCodeLoginManager()
        val flowNode = createLoginFlowNode(qrCodeLoginManager = qrCodeLoginManager)
        flowNode.observeLoginStep()
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.Initial)

        qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Starting
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.Initial)

        qrCodeLoginManager.currentLoginStep.value = QrCodeLoginStep.Finished
        assertThat(flowNode.currentNavTarget()).isEqualTo(QrCodeLoginFlowNode.NavTarget.Initial)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `startAuthentication - success`() = runTest {
        val fakeAuthenticationService = FakeMatrixAuthenticationService(
            loginWithQrCodeResult = { _, progress ->
                progress(QrCodeLoginStep.Finished)
                Result.success(A_SESSION_ID)
            }
        )
        // Test with a real manager to ensure the flow is correctly done
        val qrCodeLoginManager = DefaultQrCodeLoginManager(fakeAuthenticationService)
        val flowNode = createLoginFlowNode(
            qrCodeLoginManager = qrCodeLoginManager,
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )

        flowNode.run { startAuthentication(FakeMatrixQrCodeLoginData()) }
        assertThat(flowNode.isLoginInProgress()).isTrue()

        advanceUntilIdle()

        assertThat(qrCodeLoginManager.currentLoginStep.value).isEqualTo(QrCodeLoginStep.Finished)
        assertThat(flowNode.isLoginInProgress()).isFalse()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `startAuthentication - failure is correctly handled`() = runTest {
        val fakeAuthenticationService = FakeMatrixAuthenticationService(
            loginWithQrCodeResult = { _, progress ->
                progress(QrCodeLoginStep.Failed(QrLoginException.Unknown))
                Result.failure(IllegalStateException("Failed"))
            }
        )
        // Test with a real manager to ensure the flow is correctly done
        val qrCodeLoginManager = DefaultQrCodeLoginManager(fakeAuthenticationService)
        val flowNode = createLoginFlowNode(
            qrCodeLoginManager = qrCodeLoginManager,
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )

        flowNode.run { startAuthentication(FakeMatrixQrCodeLoginData()) }
        assertThat(flowNode.isLoginInProgress()).isTrue()

        advanceUntilIdle()

        assertThat(qrCodeLoginManager.currentLoginStep.value).isEqualTo(QrCodeLoginStep.Failed(QrLoginException.Unknown))
        assertThat(flowNode.isLoginInProgress()).isFalse()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `startAuthentication - then reset`() = runTest {
        val fakeAuthenticationService = FakeMatrixAuthenticationService(
            loginWithQrCodeResult = { _, progress ->
                progress(QrCodeLoginStep.Finished)
                Result.success(A_SESSION_ID)
            }
        )
        // Test with a real manager to ensure the flow is correctly done
        val qrCodeLoginManager = DefaultQrCodeLoginManager(fakeAuthenticationService)
        val flowNode = createLoginFlowNode(
            qrCodeLoginManager = qrCodeLoginManager,
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        )

        flowNode.run { startAuthentication(FakeMatrixQrCodeLoginData()) }
        assertThat(flowNode.isLoginInProgress()).isTrue()
        flowNode.reset()

        advanceUntilIdle()

        assertThat(qrCodeLoginManager.currentLoginStep.value).isEqualTo(QrCodeLoginStep.Uninitialized)
        assertThat(flowNode.isLoginInProgress()).isFalse()
    }

    private fun TestScope.createLoginFlowNode(
        qrCodeLoginManager: QrCodeLoginManager = FakeQrCodeLoginManager(),
        coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers()
    ): QrCodeLoginFlowNode {
        val buildContext = BuildContext(
            ancestryInfo = AncestryInfo.Root,
            savedStateMap = null,
            customisations = NodeCustomisationDirectoryImpl()
        )
        return QrCodeLoginFlowNode(
            buildContext = buildContext,
            plugins = emptyList(),
            qrCodeLoginGraphFactory = FakeQrCodeLoginGraph.Builder(qrCodeLoginManager),
            coroutineDispatchers = coroutineDispatchers,
        )
    }

    private fun QrCodeLoginFlowNode.currentNavTarget() = backstack.elements.value.last().key.navTarget
}
