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

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.login.impl.di.QrCodeLoginScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.api.auth.qrlogin.QrLoginException
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@SingleIn(QrCodeLoginScope::class)
@ContributesBinding(QrCodeLoginScope::class)
class DefaultQrCodeLoginManager @Inject constructor(
    private val authenticationService: MatrixAuthenticationService,
) : QrCodeLoginManager {
    private val _currentLoginStep = MutableStateFlow<QrCodeLoginStep>(QrCodeLoginStep.Uninitialized)
    override val currentLoginStep: StateFlow<QrCodeLoginStep> = _currentLoginStep

    override suspend fun authenticate(qrCodeLoginData: MatrixQrCodeLoginData): Result<SessionId> {
        reset()

        return authenticationService.loginWithQrCode(qrCodeLoginData) { step ->
            _currentLoginStep.value = step
        }.onFailure { throwable ->
            if (throwable is QrLoginException) {
                _currentLoginStep.value = QrCodeLoginStep.Failed(throwable)
            }
        }
    }

    override fun reset() {
        _currentLoginStep.value = QrCodeLoginStep.Uninitialized
    }
}
