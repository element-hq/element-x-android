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

    val isVerified: Boolean

    fun requestVerification()

    fun cancelVerification()

    fun approveVerification()

    fun declineVerification()

    fun startVerification()
}

sealed interface SessionVerificationServiceState {
    object Initial : SessionVerificationServiceState
    object AcceptedVerificationRequest : SessionVerificationServiceState
    object StartedSasVerification : SessionVerificationServiceState
    data class ReceivedVerificationData(val emoji: List<VerificationEmoji>) : SessionVerificationServiceState
    object Finished : SessionVerificationServiceState
    object Canceled : SessionVerificationServiceState
    object Failed : SessionVerificationServiceState
}

