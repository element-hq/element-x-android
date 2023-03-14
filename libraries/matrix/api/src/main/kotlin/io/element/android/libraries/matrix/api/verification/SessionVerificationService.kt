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
import org.matrix.rustcomponents.sdk.SessionVerificationEmoji

interface SessionVerificationService {

    val verificationAttemptStatus : StateFlow<SessionVerificationServiceState>

    /**
     * The internal service that checks verification can only run after the initial sync.
     * This [StateFlow] will notify consumers when the service is ready to be used.
     */
    val isReady: StateFlow<Boolean>

    /**
     * Exposes whether the current session is verified or not.
     */
    val isVerified: StateFlow<Boolean>

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

/** States produced by the [SessionVerificationService]. */
sealed interface SessionVerificationServiceState {
    /** Initial state. */
    object Initial : SessionVerificationServiceState

    /** Session verification request was accepted by another device. */
    object AcceptedVerificationRequest : SessionVerificationServiceState

    /** Short Authentication String (SAS) verification started between the 2 devices. */
    object StartedSasVerification : SessionVerificationServiceState

    /** Verification data for the SAS verification (emojis) received. */
    data class ReceivedVerificationData(val emoji: List<VerificationEmoji>) : SessionVerificationServiceState

    /** Verification completed successfully. */
    object Finished : SessionVerificationServiceState

    /** Verification was cancelled by either device. */
    object Canceled : SessionVerificationServiceState

    /** Verification failed with an error. */
    object Failed : SessionVerificationServiceState
}

