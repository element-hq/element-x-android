/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.verification

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SessionVerificationService {
    /**
     * State of the current verification flow ([VerificationFlowState.Initial] if not started).
     */
    val verificationFlowState: StateFlow<VerificationFlowState>

    /**
     * Returns whether the current verification status is either: [SessionVerifiedStatus.Unknown], [SessionVerifiedStatus.NotVerified]
     * or [SessionVerifiedStatus.Verified].
     */
    val sessionVerifiedStatus: StateFlow<SessionVerifiedStatus>

    /**
     * Returns whether the current session needs to be verified.
     */
    val needsSessionVerification: Flow<Boolean>

    /**
     * Request verification of the current session.
     */
    suspend fun requestCurrentSessionVerification()

    /**
     * Request verification of the user with the given [userId].
     */
    suspend fun requestUserVerification(userId: UserId)

    /**
     * Cancels the current verification attempt.
     */
    suspend fun cancelVerification()

    /**
     * Approves the current verification. This must happen on both devices to successfully verify a session.
     */
    suspend fun approveVerification()

    /**
     * Declines the verification attempt because the user could not verify or does not trust the other side of the verification.
     */
    suspend fun declineVerification()

    /**
     * Starts the verification of the unverified session from another device.
     */
    suspend fun startVerification()

    /**
     * Returns the verification service state to the initial step.
     */
    suspend fun reset(cancelAnyPendingVerificationAttempt: Boolean)

    /**
     * Register a listener to be notified of incoming session verification requests.
     */
    fun setListener(listener: SessionVerificationServiceListener?)

    /**
     * Set this particular request as the currently active one and register for
     * events pertaining it.
     */
    suspend fun acknowledgeVerificationRequest(verificationRequest: VerificationRequest.Incoming)

    /**
     * Accept the previously acknowledged verification request.
     */
    suspend fun acceptVerificationRequest()
}

interface SessionVerificationServiceListener {
    fun onIncomingSessionRequest(verificationRequest: VerificationRequest.Incoming)
}

/** Verification status of the current session. */
@Immutable
sealed interface SessionVerifiedStatus {
    /** Unknown status, we couldn't read the actual value from the SDK. */
    data object Unknown : SessionVerifiedStatus

    /** Not verified session status. */
    data object NotVerified : SessionVerifiedStatus

    /** Verified session status. */
    data object Verified : SessionVerifiedStatus

    /** Returns whether the session is [Verified]. */
    fun isVerified(): Boolean = this is Verified
}

/** States produced by the [SessionVerificationService]. */
@Immutable
sealed interface VerificationFlowState {
    /** Initial state. */
    data object Initial : VerificationFlowState

    /** Session verification request was accepted by another device. */
    data object DidAcceptVerificationRequest : VerificationFlowState

    /** Short Authentication String (SAS) verification started between the 2 devices. */
    data object DidStartSasVerification : VerificationFlowState

    /** Verification data for the SAS verification received. */
    data class DidReceiveVerificationData(val data: SessionVerificationData) : VerificationFlowState

    /** Verification completed successfully. */
    data object DidFinish : VerificationFlowState

    /** Verification was cancelled by either device. */
    data object DidCancel : VerificationFlowState

    /** Verification failed with an error. */
    data object DidFail : VerificationFlowState
}
