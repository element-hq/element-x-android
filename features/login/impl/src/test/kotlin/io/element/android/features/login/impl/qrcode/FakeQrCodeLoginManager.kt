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

import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.flow.MutableStateFlow

class FakeQrCodeLoginManager(
    var authenticateResult: (MatrixQrCodeLoginData) -> Result<SessionId> =
        lambdaRecorder<MatrixQrCodeLoginData, Result<SessionId>> { Result.success(A_SESSION_ID) },
    var resetAction: () -> Unit = lambdaRecorder<Unit> { },
) : QrCodeLoginManager {
    override val currentLoginStep: MutableStateFlow<QrCodeLoginStep> =
        MutableStateFlow(QrCodeLoginStep.Uninitialized)

    override suspend fun authenticate(qrCodeLoginData: MatrixQrCodeLoginData): Result<SessionId> {
        return authenticateResult(qrCodeLoginData)
    }

    override fun reset() {
        resetAction()
    }
}
