/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.verification

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerificationServiceListener
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSessionVerificationService(
    initialSessionVerifiedStatus: SessionVerifiedStatus = SessionVerifiedStatus.Unknown,
    private val requestCurrentSessionVerificationLambda: () -> Unit = { lambdaError() },
    private val requestUserVerificationLambda: (UserId) -> Unit = { lambdaError() },
    private val cancelVerificationLambda: () -> Unit = { lambdaError() },
    private val approveVerificationLambda: () -> Unit = { lambdaError() },
    private val declineVerificationLambda: () -> Unit = { lambdaError() },
    private val startVerificationLambda: () -> Unit = { lambdaError() },
    private val resetLambda: (Boolean) -> Unit = { lambdaError() },
    private val acknowledgeVerificationRequestLambda: (VerificationRequest.Incoming) -> Unit = { lambdaError() },
    private val acceptVerificationRequestLambda: () -> Unit = { lambdaError() },
) : SessionVerificationService {
    private val _sessionVerifiedStatus = MutableStateFlow(initialSessionVerifiedStatus)
    private var _verificationFlowState = MutableStateFlow<VerificationFlowState>(VerificationFlowState.Initial)
    private var _needsSessionVerification = MutableStateFlow(true)

    override val verificationFlowState: StateFlow<VerificationFlowState> = _verificationFlowState
    override val sessionVerifiedStatus: StateFlow<SessionVerifiedStatus> = _sessionVerifiedStatus
    override val needsSessionVerification: Flow<Boolean> = _needsSessionVerification

    override suspend fun requestCurrentSessionVerification() {
        requestCurrentSessionVerificationLambda()
    }

    override suspend fun requestUserVerification(userId: UserId) {
        requestUserVerificationLambda(userId)
    }

    override suspend fun cancelVerification() {
        cancelVerificationLambda()
    }

    override suspend fun approveVerification() {
        approveVerificationLambda()
    }

    override suspend fun declineVerification() {
        declineVerificationLambda()
    }

    override suspend fun startVerification() {
        startVerificationLambda()
    }

    override suspend fun reset(cancelAnyPendingVerificationAttempt: Boolean) {
        resetLambda(cancelAnyPendingVerificationAttempt)
    }

    var listener: SessionVerificationServiceListener? = null
        private set

    override fun setListener(listener: SessionVerificationServiceListener?) {
        this.listener = listener
    }

    override suspend fun acknowledgeVerificationRequest(verificationRequest: VerificationRequest.Incoming) {
        acknowledgeVerificationRequestLambda(verificationRequest)
    }

    override suspend fun acceptVerificationRequest() = simulateLongTask {
        acceptVerificationRequestLambda()
    }

    suspend fun emitVerificationFlowState(state: VerificationFlowState) {
        _verificationFlowState.emit(state)
    }

    suspend fun emitVerifiedStatus(status: SessionVerifiedStatus) {
        _sessionVerifiedStatus.emit(status)
    }

    suspend fun emitNeedsSessionVerification(needsVerification: Boolean) {
        _needsSessionVerification.emit(needsVerification)
    }
}
