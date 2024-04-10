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

import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.tests.testutils.lambda.LambdaOneParamRecorder
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSessionVerificationService(
    var saveVerifiedStateResult: LambdaOneParamRecorder<Boolean, Unit> = lambdaRecorder<Boolean, Unit> {}
) : SessionVerificationService {
    private val _isReady = MutableStateFlow(false)
    private val _sessionVerifiedStatus = MutableStateFlow<SessionVerifiedStatus>(SessionVerifiedStatus.Unknown)
    private var _verificationFlowState = MutableStateFlow<VerificationFlowState>(VerificationFlowState.Initial)
    private var _canVerifySessionFlow = MutableStateFlow(true)
    var shouldFail = false

    override val needsVerificationFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val verificationFlowState: StateFlow<VerificationFlowState> = _verificationFlowState
    override val sessionVerifiedStatus: StateFlow<SessionVerifiedStatus> = _sessionVerifiedStatus
    override val canVerifySessionFlow: Flow<Boolean> = _canVerifySessionFlow

    override val isReady: StateFlow<Boolean> = _isReady

    override suspend fun requestVerification() {
        if (!shouldFail) {
            _verificationFlowState.value = VerificationFlowState.AcceptedVerificationRequest
        } else {
            _verificationFlowState.value = VerificationFlowState.Failed
        }
    }

    override suspend fun cancelVerification() {
        _verificationFlowState.value = VerificationFlowState.Canceled
    }

    override suspend fun approveVerification() {
        if (!shouldFail) {
            _verificationFlowState.value = VerificationFlowState.Finished
        } else {
            _verificationFlowState.value = VerificationFlowState.Failed
        }
    }

    override suspend fun declineVerification() {
        if (!shouldFail) {
            _verificationFlowState.value = VerificationFlowState.Canceled
        } else {
            _verificationFlowState.value = VerificationFlowState.Failed
        }
    }

    fun triggerReceiveVerificationData(sessionVerificationData: SessionVerificationData) {
        _verificationFlowState.value = VerificationFlowState.ReceivedVerificationData(sessionVerificationData)
    }

    override suspend fun startVerification() {
        _verificationFlowState.value = VerificationFlowState.StartedSasVerification
    }

    fun givenVerifiedStatus(status: SessionVerifiedStatus) {
        _sessionVerifiedStatus.value = status
    }

    fun givenVerificationFlowState(state: VerificationFlowState) {
        _verificationFlowState.value = state
    }

    fun givenCanVerifySession(canVerify: Boolean) {
        _canVerifySessionFlow.value = canVerify
    }

    fun givenIsReady(value: Boolean) {
        _isReady.value = value
    }

    fun givenNeedsVerification(value: Boolean) {
        needsVerificationFlow.value = value
    }

    override suspend fun reset() {
        _verificationFlowState.value = VerificationFlowState.Initial
    }

    override suspend fun saveVerifiedState(verified: Boolean) {
        saveVerifiedStateResult(verified)
    }
}
