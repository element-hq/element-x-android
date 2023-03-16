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

package io.element.android.libraries.matrix.api.verification

import kotlinx.coroutines.flow.StateFlow

interface SessionVerificationService {

    /**
     * State of the current verification flow ([VerificationFlowState.Initial] if not started).
     */
    val verificationFlowState : StateFlow<VerificationFlowState>

    /**
     * The internal service that checks verification can only run after the initial sync.
     * This [StateFlow] will notify consumers when the service is ready to be used.
     */
    val isReady: StateFlow<Boolean>

    /**
     * Returns whether the current verification status is either: [SessionVerifiedStatus.Unknown], [SessionVerifiedStatus.NotVerified]
     * or [SessionVerifiedStatus.Verified].
     */
    val sessionVerifiedStatus: StateFlow<SessionVerifiedStatus>

    /**
     * Request verification of the current session.
     */
    fun requestVerification()

    /**
     * Cancels the current verification attempt.
     */
    fun cancelVerification()

    /**
     * Approves the current verification. This must happen on both devices to successfully verify a session.
     */
    fun approveVerification()

    /**
     * Declines the verification attempt because the user could not verify or does not trust the other side of the verification.
     */
    fun declineVerification()

    /**
     * Starts the verification of the unverified session from another device.
     */
    fun startVerification()

    /**
     * Returns the verification service state to the initial step.
     */
    fun reset()
}

/** Verification status of the current session. */
sealed interface SessionVerifiedStatus {
    /** Unknown status, we couldn't read the actual value from the SDK. */
    object Unknown : SessionVerifiedStatus

    /** Not verified session status. */
    object NotVerified : SessionVerifiedStatus

    /** Verified session status. */
    object Verified : SessionVerifiedStatus
}

/** States produced by the [SessionVerificationService]. */
sealed interface VerificationFlowState {
    /** Initial state. */
    object Initial : VerificationFlowState

    /** Session verification request was accepted by another device. */
    object AcceptedVerificationRequest : VerificationFlowState

    /** Short Authentication String (SAS) verification started between the 2 devices. */
    object StartedSasVerification : VerificationFlowState

    /** Verification data for the SAS verification (emojis) received. */
    data class ReceivedVerificationData(val emoji: List<VerificationEmoji>) : VerificationFlowState

    /** Verification completed successfully. */
    object Finished : VerificationFlowState

    /** Verification was cancelled by either device. */
    object Canceled : VerificationFlowState

    /** Verification failed with an error. */
    object Failed : VerificationFlowState
}
