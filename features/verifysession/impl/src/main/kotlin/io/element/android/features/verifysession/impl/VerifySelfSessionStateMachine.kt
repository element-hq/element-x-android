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
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import com.freeletics.flowredux.dsl.State as MachineState

@OptIn(FlowPreview::class)
class VerifySelfSessionStateMachine @Inject constructor(
    private val sessionVerificationService: SessionVerificationService,
    private val encryptionService: EncryptionService,
) : FlowReduxStateMachine<VerifySelfSessionStateMachine.State, VerifySelfSessionStateMachine.Event>(
    initialState = State.Initial
) {
    init {
        spec {
            inState<State.Initial> {
                on { _: Event.RequestVerification, state ->
                    state.override { State.RequestingVerification }
                }
                on { _: Event.StartSasVerification, state ->
                    state.override { State.StartingSasVerification }
                }
            }
            inState<State.RequestingVerification> {
                onEnterEffect {
                    sessionVerificationService.requestVerification()
                }
                on { _: Event.DidAcceptVerificationRequest, state ->
                    state.override { State.VerificationRequestAccepted }
                }
            }
            inState<State.StartingSasVerification> {
                onEnterEffect {
                    sessionVerificationService.startVerification()
                }
            }
            inState<State.VerificationRequestAccepted> {
                on { _: Event.StartSasVerification, state ->
                    state.override { State.StartingSasVerification }
                }
            }
            inState<State.Canceled> {
                on { _: Event.RequestVerification, state ->
                    state.override { State.RequestingVerification }
                }
                on { _: Event.Reset, state ->
                    state.override { State.Initial }
                }
            }
            inState<State.SasVerificationStarted> {
                on { event: Event.DidReceiveChallenge, state ->
                    state.override { State.Verifying.ChallengeReceived(event.data) }
                }
            }
            inState<State.Verifying.ChallengeReceived> {
                on { _: Event.AcceptChallenge, state ->
                    state.override { State.Verifying.Replying(state.snapshot.data, accept = true) }
                }
                on { _: Event.DeclineChallenge, state ->
                    state.override { State.Verifying.Replying(state.snapshot.data, accept = false) }
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
                on { _: Event.DidAcceptChallenge, state ->
                    // If a key backup exists, wait until it's restored or a timeout happens
                    val hasBackup = encryptionService.doesBackupExistOnServer().getOrNull().orFalse()
                    if (hasBackup) {
                        tryOrNull {
                            encryptionService.recoveryStateStateFlow.filter { it == RecoveryState.ENABLED }
                                .timeout(10.seconds)
                                .first()
                        }
                    }
                    state.override { State.Completed }
                }
            }
            inState<State.Canceling> {
                // TODO The 'Canceling' -> 'Canceled' transitions doesn't seem to work anymore, check if something changed in the Rust SDK
                onEnterEffect {
                    sessionVerificationService.cancelVerification()
                }
            }
            inState {
                on { _: Event.DidStartSasVerification, state: MachineState<State> ->
                    state.override { State.SasVerificationStarted }
                }
                on { _: Event.Cancel, state: MachineState<State> ->
                    when (state.snapshot) {
                        State.Initial, State.Completed, State.Canceled -> state.noChange()
                        // For some reason `cancelVerification` is not calling its delegate `didCancel` method so we don't pass from
                        // `Canceling` state to `Canceled` automatically anymore
                        else -> {
                            sessionVerificationService.cancelVerification()
                            state.override { State.Canceled }
                        }
                    }
                }
                on { _: Event.DidCancel, state: MachineState<State> ->
                    state.override { State.Canceled }
                }
                on { _: Event.DidFail, state: MachineState<State> ->
                    when (state.snapshot) {
                        is State.RequestingVerification -> state.override { State.Initial }
                        else -> state.override { State.Canceled }
                    }
                }
            }
        }
    }

    sealed interface State {
        /** The initial state, before verification started. */
        data object Initial : State

        /** Waiting for verification acceptance. */
        data object RequestingVerification : State

        /** Verification request accepted. Waiting for start. */
        data object VerificationRequestAccepted : State

        /** Waiting for SaS verification start. */
        data object StartingSasVerification : State

        /** A SaS verification flow has been started. */
        data object SasVerificationStarted : State

        sealed class Verifying(open val data: SessionVerificationData) : State {
            /** Verification accepted and emojis received. */
            data class ChallengeReceived(override val data: SessionVerificationData) : Verifying(data)

            /** Replying to a verification challenge. */
            data class Replying(override val data: SessionVerificationData, val accept: Boolean) : Verifying(data)
        }

        /** The verification is being canceled. */
        data object Canceling : State

        /** The verification has been canceled, remotely or locally. */
        data object Canceled : State

        /** Verification successful. */
        data object Completed : State
    }

    sealed interface Event {
        /** Request verification. */
        data object RequestVerification : Event

        /** The current verification request has been accepted. */
        data object DidAcceptVerificationRequest : Event

        /** Start a SaS verification flow. */
        data object StartSasVerification : Event

        /** Started a SaS verification flow. */
        data object DidStartSasVerification : Event

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

        /** Reset the verification flow to the initial state. */
        data object Reset : Event
    }
}
