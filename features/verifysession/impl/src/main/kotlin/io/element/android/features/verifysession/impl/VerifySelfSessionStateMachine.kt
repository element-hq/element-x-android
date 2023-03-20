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

import io.element.android.libraries.core.statemachine.createStateMachine
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.VerificationEmoji
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class VerifySelfSessionStateMachine(
    coroutineScope: CoroutineScope,
    private val sessionVerificationService: SessionVerificationService,
) {

    private val stateMachine = createStateMachine {
        logger = { message -> Timber.d(message) }

        addInitialState(State.Initial) {
            on<Event.RequestVerification>(State.RequestingVerification)
            on<Event.StartSasVerification>(State.StartingSasVerification)
        }
        addState<State.RequestingVerification> {
            onEnter { sessionVerificationService.requestVerification() }

            on<Event.DidAcceptVerificationRequest>(State.VerificationRequestAccepted)
            on<Event.DidFail>(State.Initial)
        }
        addState<State.StartingSasVerification> {
            onEnter { sessionVerificationService.startVerification() }
        }
        addState<State.VerificationRequestAccepted> {
            on<Event.StartSasVerification>(State.StartingSasVerification)
        }
        addState<State.Canceled> {
            on<Event.Restart>(State.RequestingVerification)
        }
        addState<State.SasVerificationStarted> {
            on<Event.DidReceiveChallenge> { event, _ -> State.Verifying.ChallengeReceived(event.emojis) }
        }
        addState<State.Verifying.ChallengeReceived> {
            on<Event.AcceptChallenge> { _, prevState -> State.Verifying.Replying(prevState.emojis, true) }
            on<Event.DeclineChallenge> { _, prevState -> State.Verifying.Replying(prevState.emojis, false) }
        }
        addState<State.Verifying.Replying> {
            onEnter { state ->
                if (state.accept) {
                    sessionVerificationService.approveVerification()
                } else {
                    sessionVerificationService.declineVerification()
                }
            }
            on<Event.DidAcceptChallenge>(State.Completed)
        }
        addState<State.Canceling> {
            onEnter { sessionVerificationService.cancelVerification() }
        }
        on<Event.DidStartSasVerification>(State.SasVerificationStarted)
        on<Event.Cancel>(State.Canceling)
        on<Event.DidCancel>(State.Canceled)
        on<Event.DidFail>(State.Canceled)
    }

    init {
        // Observe the verification service state, translate it to state machine input events
        sessionVerificationService.verificationFlowState.onEach { verificationAttemptState ->
            when (verificationAttemptState) {
                VerificationFlowState.AcceptedVerificationRequest -> {
                    stateMachine.process(Event.DidAcceptVerificationRequest)
                }
                VerificationFlowState.StartedSasVerification -> {
                    stateMachine.process(Event.DidStartSasVerification)
                }
                is VerificationFlowState.ReceivedVerificationData -> {
                    // For some reason we receive this state twice, we need to discard the 2nd one
                    if (stateMachine.currentState == State.SasVerificationStarted) {
                        stateMachine.process(Event.DidReceiveChallenge(verificationAttemptState.emoji))
                    }
                }
                VerificationFlowState.Finished -> {
                    stateMachine.process(Event.DidAcceptChallenge)
                }
                VerificationFlowState.Canceled -> {
                    stateMachine.process(Event.DidCancel)
                }
                VerificationFlowState.Failed -> {
                    stateMachine.process(Event.DidFail)
                }
                else -> Unit
            }
        }.launchIn(coroutineScope)
    }

    val state: StateFlow<State> = stateMachine.stateFlow

    fun process(event: Event) = stateMachine.process(event)

    sealed interface State {
        /** The initial state, before verification started. */
        object Initial : State

        /** Waiting for verification acceptance. */
        object RequestingVerification : State

        /** Verification request accepted. Waiting for start. */
        object VerificationRequestAccepted : State

        /** Waiting for SaS verification start. */
        object StartingSasVerification : State

        /** A SaS verification flow has been started. */
        object SasVerificationStarted : State

        sealed class Verifying(open val emojis: List<VerificationEmoji>) : State {
            /** Verification accepted and emojis received. */
            data class ChallengeReceived(override val emojis: List<VerificationEmoji>) : Verifying(emojis)

            /** Replying to a verification challenge. */
            data class Replying(override val emojis: List<VerificationEmoji>, val accept: Boolean) : Verifying(emojis)
        }
        /** The verification is being canceled. */
        object Canceling : State
        /** The verification has been canceled, remotely or locally. */
        object Canceled : State
        /** Verification successful. */
        object Completed : State
    }

    sealed interface Event {
        /** Request verification. */
        object RequestVerification : Event
        /** The current verification request has been accepted. */
        object DidAcceptVerificationRequest : Event
        /** Start a SaS verification flow. */
        object StartSasVerification : Event
        /** Started a SaS verification flow. */
        object DidStartSasVerification : Event
        /** Has received emojis. */
        data class DidReceiveChallenge(val emojis: List<VerificationEmoji>) : Event
        /** Emojis match. */
        object AcceptChallenge : Event
        /** Emojis do not match. */
        object DeclineChallenge : Event
        /** Remote accepted challenge. */
        object DidAcceptChallenge : Event
        /** Request cancellation. */
        object Cancel : Event
        /** Verification cancelled. */
        object DidCancel : Event
        /** Request failed. */
        object DidFail : Event
        /** Restart the verification flow. */
        object Restart : Event
    }
}
