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

import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerificationServiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.matrix.rustcomponents.sdk.SessionVerificationController
import org.matrix.rustcomponents.sdk.SessionVerificationControllerDelegate
import org.matrix.rustcomponents.sdk.SessionVerificationEmoji
import javax.inject.Inject

class MatrixSessionVerificationService @Inject constructor(
    private val verificationController: SessionVerificationController,
) : SessionVerificationService, SessionVerificationControllerDelegate {

    private val _verificationAttemptStatus = MutableStateFlow<SessionVerificationServiceState>(SessionVerificationServiceState.Initial)
    override val verificationAttemptStatus = _verificationAttemptStatus.asStateFlow()

    init {
        verificationController.setDelegate(this)
    }

    override val isVerified: Boolean get() = verificationController.isVerified()

    override fun requestVerification() = tryOrFail { verificationController.requestVerification() }

    override fun cancelVerification() = tryOrFail { verificationController.cancelVerification() }

    override fun approveVerification() = tryOrFail { verificationController.approveVerification() }

    override fun declineVerification() = tryOrFail { verificationController.declineVerification() }

    override fun startVerification() = tryOrFail { verificationController.startSasVerification() }

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
    }

    override fun didFail() {
        _verificationAttemptStatus.value = SessionVerificationServiceState.Failed
    }

    override fun didFinish() {
        _verificationAttemptStatus.value = SessionVerificationServiceState.Finished
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
