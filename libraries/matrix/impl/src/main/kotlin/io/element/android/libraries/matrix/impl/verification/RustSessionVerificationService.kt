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

import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.api.verification.VerificationEmoji
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.matrix.rustcomponents.sdk.SessionVerificationController
import org.matrix.rustcomponents.sdk.SessionVerificationControllerDelegate
import org.matrix.rustcomponents.sdk.SessionVerificationControllerInterface
import org.matrix.rustcomponents.sdk.SessionVerificationEmoji
import javax.inject.Inject

class RustSessionVerificationService @Inject constructor() : SessionVerificationService, SessionVerificationControllerDelegate {

    var verificationController: SessionVerificationControllerInterface? = null
        set(value) {
            field = value
            _isReady.value = value != null
            // If status was 'Unknown', move it to either 'Verified' or 'NotVerified'
            if (value != null) {
                value.setDelegate(this)
                updateVerificationStatus(value.isVerified())
            }
        }

    private val _verificationFlowState = MutableStateFlow<VerificationFlowState>(VerificationFlowState.Initial)
    override val verificationFlowState = _verificationFlowState.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    override val isReady = _isReady.asStateFlow()

    private val _sessionVerifiedStatus = MutableStateFlow<SessionVerifiedStatus>(SessionVerifiedStatus.Unknown)
    override val sessionVerifiedStatus: StateFlow<SessionVerifiedStatus> = _sessionVerifiedStatus.asStateFlow()

    override suspend fun requestVerification() = tryOrFail {
        verificationController?.requestVerification()
    }

    override suspend fun cancelVerification() = tryOrFail { verificationController?.cancelVerification() }

    override suspend fun approveVerification() = tryOrFail { verificationController?.approveVerification() }

    override suspend fun declineVerification() = tryOrFail { verificationController?.declineVerification() }

    override suspend fun startVerification() = tryOrFail {
        verificationController?.startSasVerification()
    }

    private suspend fun tryOrFail(block: suspend () -> Unit) {
        runCatching {
            block()
        }.onFailure { didFail() }
    }

    // region Delegate implementation

    // When verification attempt is accepted by the other device
    override fun didAcceptVerificationRequest() {
        _verificationFlowState.value = VerificationFlowState.AcceptedVerificationRequest
    }

    override fun didCancel() {
        _verificationFlowState.value = VerificationFlowState.Canceled
    }

    override fun didFail() {
        _verificationFlowState.value = VerificationFlowState.Failed
    }

    override fun didFinish() {
        _verificationFlowState.value = VerificationFlowState.Finished
        // Ideally this should be `verificationController?.isVerified().orFalse()` but for some reason it always returns false
        updateVerificationStatus(isVerified = true)
    }

    override fun didReceiveVerificationData(data: List<SessionVerificationEmoji>) {
        val emojis = data.map { emoji ->
            emoji.use { VerificationEmoji(it.symbol(), it.description()) }
        }
        _verificationFlowState.value = VerificationFlowState.ReceivedVerificationData(emojis)
    }

    // When the actual SAS verification starts
    override fun didStartSasVerification() {
        _verificationFlowState.value = VerificationFlowState.StartedSasVerification
    }

    // end-region

    override suspend fun reset() {
        if (isReady.value) {
            // Cancel any pending verification attempt
            tryOrNull { verificationController?.cancelVerification() }
        }
        _verificationFlowState.value = VerificationFlowState.Initial
    }

    fun destroy() {
        (verificationController as? SessionVerificationController)?.destroy()
        verificationController = null
    }

    private fun updateVerificationStatus(isVerified: Boolean) {
        val newValue = when {
            !isReady.value -> SessionVerifiedStatus.Unknown
            !isVerified -> SessionVerifiedStatus.NotVerified
            else -> SessionVerifiedStatus.Verified
        }
        _sessionVerifiedStatus.value = newValue
    }
}
