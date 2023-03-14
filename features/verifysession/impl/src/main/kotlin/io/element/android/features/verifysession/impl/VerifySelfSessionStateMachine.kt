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

@file:Suppress("WildcardImport")
package io.element.android.features.verifysession.impl

import io.element.android.features.verifysession.impl.SessionVerificationEvent.*
import io.element.android.features.verifysession.impl.SessionVerificationState.*
import io.element.android.libraries.core.statemachine.createStateMachine
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerificationServiceState
import io.element.android.libraries.matrix.api.verification.VerificationEmoji
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class VerifySelfSessionStateMachine(
    coroutineScope: CoroutineScope,
    private val sessionVerificationService: SessionVerificationService,
) {

    private val stateMachine = createStateMachine {
        addInitialState(Initial) {
            on<RequestVerification>(RequestingVerification)
            on<StartSasVerification>(StartingSasVerification)
        }
        addState<RequestingVerification> {
            onEnter { sessionVerificationService.requestVerification() }

            on<DidAcceptVerificationRequest>(VerificationRequestAccepted)
            on<DidFail>(Initial)
        }
        addState<StartingSasVerification> {
            onEnter { sessionVerificationService.startVerification() }
        }
        addState<VerificationRequestAccepted> {
            on<StartSasVerification>(StartingSasVerification)
        }
        addState<Canceled> {
            on<Restart>(RequestingVerification)
        }
        addState<SasVerificationStarted> {
            on<DidReceiveChallenge> { event, _ -> Verifying.ChallengeReceived(event.emojis) }
        }
        addState<Verifying.ChallengeReceived> {
            on<AcceptChallenge> { _, prevState -> Verifying.Replying(prevState.emojis, true) }
            on<DeclineChallenge> { _, prevState -> Verifying.Replying(prevState.emojis, false) }
        }
        addState<Verifying.Replying> {
            onEnter { state ->
                if (state.accept) {
                    sessionVerificationService.approveVerification()
                } else {
                    sessionVerificationService.declineVerification()
                }
            }
            on<DidAcceptChallenge>(Completed)
        }
        addState<Canceling> {
            onEnter { sessionVerificationService.cancelVerification() }
        }
        on<DidStartSasVerification>(SasVerificationStarted)
        on<Cancel>(Canceling)
        on<DidCancel>(Canceled)
        on<DidFail>(Canceled)
    }

    init {
        // Observe the verification service state, translate it to state machine input events
        sessionVerificationService.verificationAttemptStatus.onEach { verificationAttemptState ->
            when (verificationAttemptState) {
                SessionVerificationServiceState.AcceptedVerificationRequest -> {
                    stateMachine.process(DidAcceptVerificationRequest)
                }
                SessionVerificationServiceState.StartedSasVerification -> {
                    stateMachine.process(DidStartSasVerification)
                }
                is SessionVerificationServiceState.ReceivedVerificationData -> {
                    // For some reason we receive this state twice, we need to discard the 2nd one
                    if (stateMachine.currentState == SasVerificationStarted) {
                        stateMachine.process(DidReceiveChallenge(verificationAttemptState.emoji))
                    }
                }
                SessionVerificationServiceState.Finished -> {
                    stateMachine.process(DidAcceptChallenge)
                }
                SessionVerificationServiceState.Canceled -> {
                    stateMachine.process(DidCancel)
                }
                SessionVerificationServiceState.Failed -> {
                    stateMachine.process(DidFail)
                }
                else -> Unit
            }
        }.launchIn(coroutineScope)
    }

    val state: StateFlow<SessionVerificationState> = stateMachine.stateFlow

    fun process(event: SessionVerificationEvent) = stateMachine.process(event)
}
sealed interface SessionVerificationEvent {
    /** Request verification. */
    object RequestVerification : SessionVerificationEvent
    /** The current verification request has been accepted. */
    object DidAcceptVerificationRequest : SessionVerificationEvent
    /** Start a SaS verification flow. */
    object StartSasVerification : SessionVerificationEvent
    /** Started a SaS verification flow. */
    object DidStartSasVerification : SessionVerificationEvent
    /** Has received emojis. */
    data class DidReceiveChallenge(val emojis: List<VerificationEmoji>) : SessionVerificationEvent
    /** Emojis match. */
    object AcceptChallenge : SessionVerificationEvent
    /** Emojis do not match. */
    object DeclineChallenge : SessionVerificationEvent
    /** Remote accepted challenge. */
    object DidAcceptChallenge : SessionVerificationEvent
    /** Request cancellation. */
    object Cancel : SessionVerificationEvent
    /** Verification cancelled. */
    object DidCancel : SessionVerificationEvent
    /** Request failed. */
    object DidFail : SessionVerificationEvent
    /** Restart the verification flow. */
    object Restart : SessionVerificationEvent
}

sealed interface SessionVerificationState {
    /** The initial state, before verification started. */
    object Initial : SessionVerificationState

    /** Waiting for verification acceptance. */
    object RequestingVerification : SessionVerificationState

    /** Verification request accepted. Waiting for start. */
    object VerificationRequestAccepted : SessionVerificationState

    /** Waiting for SaS verification start. */
    object StartingSasVerification : SessionVerificationState

    /** A SaS verification flow has been started. */
    object SasVerificationStarted : SessionVerificationState

    sealed class Verifying(open val emojis: List<VerificationEmoji>) : SessionVerificationState {
        /** Verification accepted and emojis received. */
        data class ChallengeReceived(override val emojis: List<VerificationEmoji>) : Verifying(emojis)

        /** Replying to a verification challenge. */
        data class Replying(override val emojis: List<VerificationEmoji>, val accept: Boolean) : Verifying(emojis)
    }
    /** The verification is being canceled. */
    object Canceling : SessionVerificationState
    /** The verification has been canceled, remotely or locally. */
    object Canceled : SessionVerificationState
    /** Verification successful. */
    object Completed : SessionVerificationState
}
