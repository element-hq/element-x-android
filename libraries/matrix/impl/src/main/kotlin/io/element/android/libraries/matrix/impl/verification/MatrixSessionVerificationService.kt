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

package io.element.android.libraries.matrix.impl.verification

import io.element.android.libraries.core.bool.orTrue
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerificationServiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.matrix.rustcomponents.sdk.SessionVerificationController
import org.matrix.rustcomponents.sdk.SessionVerificationControllerDelegate
import org.matrix.rustcomponents.sdk.SessionVerificationEmoji
import javax.inject.Inject

class MatrixSessionVerificationService @Inject constructor() : SessionVerificationService, SessionVerificationControllerDelegate {

    var verificationController: SessionVerificationController? = null
        set(value) {
            field = value
            _isReady.value = value != null
        }

    private val _verificationAttemptStatus = MutableStateFlow<SessionVerificationServiceState>(SessionVerificationServiceState.Initial)
    override val verificationAttemptStatus = _verificationAttemptStatus.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    override val isReady = _isReady.asStateFlow()

    override val isVerified: Boolean get() = verificationController?.isVerified().orTrue()

    override fun requestVerification() = tryOrFail {
        verificationController?.setDelegate(this)
        verificationController?.requestVerification()
    }

    override fun cancelVerification() = tryOrFail { verificationController?.cancelVerification() }

    override fun approveVerification() = tryOrFail { verificationController?.approveVerification() }

    override fun declineVerification() = tryOrFail { verificationController?.declineVerification() }

    override fun startVerification() = tryOrFail {
        verificationController?.setDelegate(this)
        verificationController?.startSasVerification()
    }

    private fun tryOrFail(block: () -> Unit) {
        runCatching {
            block()
        }.onFailure { didFail() }
    }

    // region Delegate implementation

    // When verification attempt is accepted by the other device
    override fun didAcceptVerificationRequest() {
        _verificationAttemptStatus.value = SessionVerificationServiceState.AcceptedVerificationRequest
    }

    override fun didCancel() {
        _verificationAttemptStatus.value = SessionVerificationServiceState.Canceled
        verificationController?.setDelegate(null)
    }

    override fun didFail() {
        _verificationAttemptStatus.value = SessionVerificationServiceState.Failed
        verificationController?.setDelegate(null)
    }

    override fun didFinish() {
        _verificationAttemptStatus.value = SessionVerificationServiceState.Finished
        verificationController?.setDelegate(null)
    }

    override fun didReceiveVerificationData(data: List<SessionVerificationEmoji>) {
        _verificationAttemptStatus.value = SessionVerificationServiceState.ReceivedVerificationData(data)
    }

    // When the actual SAS verification starts
    override fun didStartSasVerification() {
        _verificationAttemptStatus.value = SessionVerificationServiceState.StartedSasVerification
    }

    // end-region
}
