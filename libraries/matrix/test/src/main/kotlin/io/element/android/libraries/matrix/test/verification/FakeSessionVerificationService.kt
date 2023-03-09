/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.matrix.test.verification

import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerificationServiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSessionVerificationService : SessionVerificationService {
    private var _isVerified = false
    private var _verificationAttemptStatus = MutableStateFlow<SessionVerificationServiceState>(SessionVerificationServiceState.Initial)

    override val verificationAttemptStatus: StateFlow<SessionVerificationServiceState>
        get() = _verificationAttemptStatus
    override val isVerified: Boolean get()= _isVerified

    override fun requestVerification() = Unit

    override fun cancelVerification() = Unit

    override fun approveVerification() = Unit

    override fun declineVerification() = Unit

    override fun startVerification() = Unit

    fun givenIsVerified(value: Boolean) {
        _isVerified = value
    }

    fun givenVerificationAttemptStatus(state: SessionVerificationServiceState) {
        _verificationAttemptStatus.value = state
    }
}
