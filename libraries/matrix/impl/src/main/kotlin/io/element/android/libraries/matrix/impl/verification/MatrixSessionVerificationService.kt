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
import io.element.android.libraries.matrix.api.verification.VerificationAttemptState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.matrix.rustcomponents.sdk.SessionVerificationController
import org.matrix.rustcomponents.sdk.SessionVerificationControllerDelegate
import org.matrix.rustcomponents.sdk.SessionVerificationEmoji
import javax.inject.Inject

class MatrixSessionVerificationService @Inject constructor(
    private val verificationController: SessionVerificationController,
) : SessionVerificationService, SessionVerificationControllerDelegate {

    private val _verificationAttemptStatus = MutableStateFlow<VerificationAttemptState>(VerificationAttemptState.Initial)
    override val verificationAttemptStatus = _verificationAttemptStatus.asStateFlow()

    init {
        verificationController.setDelegate(this)
    }

    override val isVerified: Boolean get() = verificationController.isVerified()

    override fun requestVerification() = tryOrFail {
        _verificationAttemptStatus.value = VerificationAttemptState.RequestingVerification
        verificationController.requestVerification()
    }

    override fun cancelVerification() = tryOrFail {
        verificationController.cancelVerification()
        if (verificationAttemptStatus.value == VerificationAttemptState.RequestingVerification) {
            _verificationAttemptStatus.value = VerificationAttemptState.Canceled
        }
    }

    override fun approveVerification() = tryOrFail {
        verificationController.approveVerification()
        val emojis = (verificationAttemptStatus.value as? VerificationAttemptState.Verifying)?.emojis
            ?: error("Invalid state: ${_verificationAttemptStatus.value}")
        _verificationAttemptStatus.value = VerificationAttemptState.Verifying.Replying(emojis)
    }

    override fun declineVerification() = tryOrFail {
        verificationController.declineVerification()
        val emojis = (verificationAttemptStatus.value as? VerificationAttemptState.Verifying)?.emojis
            ?: error("Invalid state: ${_verificationAttemptStatus.value}")
        _verificationAttemptStatus.value = VerificationAttemptState.Verifying.Replying(emojis)
    }

    override fun startVerification() = tryOrFail { verificationController.startSasVerification() }

    private fun tryOrFail(block: () -> Unit) {
        runCatching {
            block()
        }.onFailure { didFail() }
    }

    // region Delegate implementation

    // When verification attempt is accepted by the other device
    override fun didAcceptVerificationRequest() {
        println("Accepted")
    }

    override fun didCancel() {
        _verificationAttemptStatus.value = VerificationAttemptState.Canceled
    }

    override fun didFail() {
        _verificationAttemptStatus.value = VerificationAttemptState.Failed
    }

    override fun didFinish() {
        _verificationAttemptStatus.value = VerificationAttemptState.Completed
    }

    override fun didReceiveVerificationData(data: List<SessionVerificationEmoji>) {
        _verificationAttemptStatus.value = VerificationAttemptState.Verifying.ChallengeReceived(data)
    }

    // When the actual SAS verification starts
    override fun didStartSasVerification() {
        println("Started")
    }

    // end-region
}
