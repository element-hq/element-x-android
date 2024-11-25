/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.verifysession.impl.incoming

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import io.element.android.features.verifysession.impl.util.andLogStateChange
import io.element.android.features.verifysession.impl.util.logReceivedEvents
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import com.freeletics.flowredux.dsl.State as MachineState

class IncomingVerificationStateMachine @Inject constructor(
    private val sessionVerificationService: SessionVerificationService,
) : FlowReduxStateMachine<IncomingVerificationStateMachine.State, IncomingVerificationStateMachine.Event>(
    initialState = State.Initial(isCancelled = false)
) {
    init {
        spec {
            inState<State.Initial> {
                on { _: Event.AcceptIncomingRequest, state ->
                    state.override { State.AcceptingIncomingVerification.andLogStateChange() }
                }
            }
            inState<State.AcceptingIncomingVerification> {
                onEnterEffect {
                    sessionVerificationService.acceptVerificationRequest()
                }
                on { event: Event.DidReceiveChallenge, state ->
                    state.override { State.ChallengeReceived(event.data).andLogStateChange() }
                }
            }
            inState<State.ChallengeReceived> {
                on { _: Event.AcceptChallenge, state ->
                    state.override { State.AcceptingChallenge(state.snapshot.data).andLogStateChange() }
                }
                on { _: Event.DeclineChallenge, state ->
                    state.override { State.RejectingChallenge(state.snapshot.data).andLogStateChange() }
                }
            }
            inState<State.AcceptingChallenge> {
                onEnterEffect { _ ->
                    sessionVerificationService.approveVerification()
                }
                on { _: Event.DidAcceptChallenge, state ->
                    state.override { State.Completed.andLogStateChange() }
                }
            }
            inState<State.RejectingChallenge> {
                onEnterEffect { _ ->
                    sessionVerificationService.declineVerification()
                }
            }
            inState<State.Canceling> {
                onEnterEffect {
                    sessionVerificationService.cancelVerification()
                }
            }
            inState {
                logReceivedEvents()
                on { _: Event.Cancel, state: MachineState<State> ->
                    when (state.snapshot) {
                        State.Completed, State.Canceled -> state.noChange()
                        else -> {
                            sessionVerificationService.cancelVerification()
                            state.override { State.Canceled.andLogStateChange() }
                        }
                    }
                }
                on { _: Event.DidCancel, state: MachineState<State> ->
                    when (state.snapshot) {
                        is State.RejectingChallenge -> {
                            state.override { State.Failure.andLogStateChange() }
                        }
                        is State.Initial -> state.mutate { State.Initial(isCancelled = true).andLogStateChange() }
                        State.AcceptingIncomingVerification,
                        State.RejectingIncomingVerification,
                        is State.ChallengeReceived,
                        is State.AcceptingChallenge,
                        State.Canceling -> state.override { State.Canceled.andLogStateChange() }
                        State.Canceled,
                        State.Completed,
                        State.Failure -> state.noChange()
                    }
                }
                on { _: Event.DidFail, state: MachineState<State> ->
                    state.override { State.Failure.andLogStateChange() }
                }
            }
        }
    }

    sealed interface State {
        /** The initial state, before verification started. */
        data class Initial(val isCancelled: Boolean) : State

        /** User is accepting the incoming verification. */
        data object AcceptingIncomingVerification : State

        /** User is rejecting the incoming verification. */
        data object RejectingIncomingVerification : State

        /** Verification accepted and emojis received. */
        data class ChallengeReceived(val data: SessionVerificationData) : State

        /** Accepting the verification challenge. */
        data class AcceptingChallenge(val data: SessionVerificationData) : State

        /** Rejecting the verification challenge. */
        data class RejectingChallenge(val data: SessionVerificationData) : State

        /** The verification is being canceled. */
        data object Canceling : State

        /** The verification has been canceled, remotely or locally. */
        data object Canceled : State

        /** Verification successful. */
        data object Completed : State

        /** Verification failure. */
        data object Failure : State
    }

    sealed interface Event {
        /** User accepts the incoming request. */
        data object AcceptIncomingRequest : Event

        /** Has received data. */
        data class DidReceiveChallenge(val data: SessionVerificationData) : Event

        /** Emojis match. */
        data object AcceptChallenge : Event

        /** Emojis do not match. */
        data object DeclineChallenge : Event

        /** Remote accepted challenge. */
        data object DidAcceptChallenge : Event

        /** Request cancellation. */
        data object Cancel : Event

        /** Verification cancelled. */
        data object DidCancel : Event

        /** Request failed. */
        data object DidFail : Event
    }
}
