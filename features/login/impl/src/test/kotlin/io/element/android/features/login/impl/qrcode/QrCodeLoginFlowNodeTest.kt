/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.login.impl.qrcode

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.AncestryInfo
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.utils.customisations.NodeCustomisationDirectoryImpl
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.DefaultLoginUserStory
import io.element.android.features.login.impl.screens.qrcode.confirmation.QrCodeConfirmationStep
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.flow.MutableStateFlow
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

    private fun createLoginFlowNode(
        qrCodeLoginManager: FakeQrCodeLoginManager = FakeQrCodeLoginManager(),
        defaultLoginUserStory: DefaultLoginUserStory = DefaultLoginUserStory(),
    ): QrCodeLoginFlowNode {
        val buildContext = BuildContext(
            ancestryInfo = AncestryInfo.Root,
            savedStateMap = null,
            customisations = NodeCustomisationDirectoryImpl()
        )
        return QrCodeLoginFlowNode(
            buildContext = buildContext,
            plugins = emptyList(),
            qrCodeLoginManager = qrCodeLoginManager,
            defaultLoginUserStory = defaultLoginUserStory
        )
    }

    private fun QrCodeLoginFlowNode.currentNavTarget() = backstack.elements.value.last().key.navTarget
}

class FakeQrCodeLoginManager(
    var authenticateResult: (MatrixQrCodeLoginData) -> Result<SessionId> =
        lambdaRecorder<MatrixQrCodeLoginData, Result<SessionId>> { Result.success(A_SESSION_ID) },
) : QrCodeLoginManager {
    override val currentLoginStep: MutableStateFlow<QrCodeLoginStep> = MutableStateFlow(QrCodeLoginStep.Uninitialized)

    override suspend fun authenticate(qrCodeLoginData: MatrixQrCodeLoginData): Result<SessionId> {
        return authenticateResult(qrCodeLoginData)
    }
}
