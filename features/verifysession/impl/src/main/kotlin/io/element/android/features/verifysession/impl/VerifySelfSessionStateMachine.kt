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
@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.verifysession.impl

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.VerificationEmoji
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import com.freeletics.flowredux.dsl.State as MachineState

class VerifySelfSessionStateMachine @Inject constructor(
    private val sessionVerificationService: SessionVerificationService,
) : FlowReduxStateMachine<VerifySelfSessionStateMachine.State, VerifySelfSessionStateMachine.Event>(
    initialState = State.Initial
) {

    init {
        spec {
            inState<State.Initial> {
                on { _: Event.RequestVerification, state: MachineState<State.Initial> ->
                    state.override { State.RequestingVerification }
                }
                on { _: Event.StartSasVerification, state: MachineState<State.Initial> ->
                    state.override { State.StartingSasVerification }
                }
            }
            inState<State.RequestingVerification> {
                onEnterEffect {
                    sessionVerificationService.requestVerification()
                }
                on { _: Event.DidAcceptVerificationRequest, state: MachineState<State.RequestingVerification> ->
                    state.override { State.VerificationRequestAccepted }
                }
                on { _: Event.DidFail, state: MachineState<State.RequestingVerification> ->
                    state.override { State.Initial }
                }
            }
            inState<State.StartingSasVerification> {
                onEnterEffect {
                    sessionVerificationService.startVerification()
                }
            }
            inState<State.VerificationRequestAccepted> {
                on { _: Event.StartSasVerification, state: MachineState<State.VerificationRequestAccepted> ->
                    state.override { State.StartingSasVerification }
                }
            }
            inState<State.Canceled> {
                on { _: Event.Restart, state: MachineState<State.Canceled> ->
                    state.override { State.RequestingVerification }
                }
            }
            inState<State.SasVerificationStarted> {
                on { event: Event.DidReceiveChallenge, state: MachineState<State.SasVerificationStarted> ->
                    state.override { State.Verifying.ChallengeReceived(event.emojis) }
                }
            }
            inState<State.Verifying.ChallengeReceived> {
                on { _: Event.AcceptChallenge, state: MachineState<State.Verifying.ChallengeReceived> ->
                    state.override { State.Verifying.Replying(state.snapshot.emojis, accept = true) }
                }
                on { _: Event.DeclineChallenge, state: MachineState<State.Verifying.ChallengeReceived> ->
                    state.override { State.Verifying.Replying(state.snapshot.emojis, accept = false) }
                }
            }
            inState<State.Verifying.Replying> {
                onEnterEffect { state ->
                    if (state.accept) {
                        sessionVerificationService.approveVerification()
                    } else {
                        sessionVerificationService.declineVerification()
                    }
                }
                on { _: Event.DidAcceptChallenge, state: MachineState<State.Verifying.Replying> ->
                    state.override { State.Completed }
                }
            }
            inState<State.Canceling> {
                onEnterEffect {
                    sessionVerificationService.cancelVerification()
                }
            }
            inState {
                on { _: Event.DidStartSasVerification, state: MachineState<State> ->
                    state.override { State.SasVerificationStarted }
                }
                on { _: Event.Cancel, state: MachineState<State> ->
                    if (state.snapshot in sequenceOf(
                            State.Initial,
                            State.Completed,
                            State.Canceled
                        )) {
                        state.noChange()
                    } else {
                        state.override { State.Canceling }
                    }
                }
                on { _: Event.DidCancel, state: MachineState<State> ->
                    state.override { State.Canceled }
                }
                on { _: Event.DidFail, state: MachineState<State> ->
                    state.override { State.Canceled }
                }
            }
        }
    }

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
