/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.verification

import androidx.compose.runtime.Immutable
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
    suspend fun requestVerification()

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
    suspend fun reset()
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
    data object AcceptedVerificationRequest : VerificationFlowState

    /** Short Authentication String (SAS) verification started between the 2 devices. */
    data object StartedSasVerification : VerificationFlowState

    /** Verification data for the SAS verification received. */
    data class ReceivedVerificationData(val data: SessionVerificationData) : VerificationFlowState

    /** Verification completed successfully. */
    data object Finished : VerificationFlowState

    /** Verification was cancelled by either device. */
    data object Canceled : VerificationFlowState

    /** Verification failed with an error. */
    data object Failed : VerificationFlowState
}
