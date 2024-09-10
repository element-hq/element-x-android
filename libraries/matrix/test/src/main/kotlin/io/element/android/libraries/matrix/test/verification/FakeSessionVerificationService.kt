/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.verification

import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSessionVerificationService(
    initialSessionVerifiedStatus: SessionVerifiedStatus = SessionVerifiedStatus.Unknown,
) : SessionVerificationService {
    private val _sessionVerifiedStatus = MutableStateFlow(initialSessionVerifiedStatus)
    private var _verificationFlowState = MutableStateFlow<VerificationFlowState>(VerificationFlowState.Initial)
    private var _needsSessionVerification = MutableStateFlow(true)
    var shouldFail = false

    override val verificationFlowState: StateFlow<VerificationFlowState> = _verificationFlowState
    override val sessionVerifiedStatus: StateFlow<SessionVerifiedStatus> = _sessionVerifiedStatus
    override val needsSessionVerification: Flow<Boolean> = _needsSessionVerification

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

    suspend fun emitVerifiedStatus(status: SessionVerifiedStatus) {
        _sessionVerifiedStatus.emit(status)
    }

    fun givenVerificationFlowState(state: VerificationFlowState) {
        _verificationFlowState.value = state
    }

    fun givenNeedsSessionVerification(needsVerification: Boolean) {
        _needsSessionVerification.value = needsVerification
    }

    override suspend fun reset() {
        _verificationFlowState.value = VerificationFlowState.Initial
    }
}
