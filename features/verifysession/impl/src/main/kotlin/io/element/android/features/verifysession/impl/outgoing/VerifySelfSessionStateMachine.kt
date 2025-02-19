/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("WildcardImport")
@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.verifysession.impl.outgoing

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import io.element.android.features.verifysession.impl.util.andLogStateChange
import io.element.android.features.verifysession.impl.util.logReceivedEvents
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import com.freeletics.flowredux.dsl.State as MachineState

@OptIn(FlowPreview::class)
class VerifySelfSessionStateMachine(
    private val sessionVerificationService: SessionVerificationService,
    private val encryptionService: EncryptionService,
    private val verificationType: VerificationType,
) : FlowReduxStateMachine<VerifySelfSessionStateMachine.State, VerifySelfSessionStateMachine.Event>(
    initialState = initialState(sessionVerificationService)
) {
    init {
        spec {
            inState<State.Initial> {
                on { _: Event.UseAnotherDevice, state ->
                    state.override { State.UseAnotherDevice.andLogStateChange() }
                }
            }
            inState<State.UseAnotherDevice> {
                onActionEffect<Event.RequestVerification> { event, state ->
                    when (event.verificationRequest) {
                        is VerificationRequest.Outgoing.CurrentSession -> sessionVerificationService.requestCurrentSessionVerification()
                        is VerificationRequest.Outgoing.User -> sessionVerificationService.requestUserVerification(event.verificationRequest.userId)
                    }
                }
                on { event: Event.RequestVerification, state ->
                    state.override { State.RequestingVerification(event.verificationRequest).andLogStateChange() }
                }
            }
            inState<State.RequestingVerification> {
                onEnterEffect { state ->
                    when (state.verificationRequest) {
                        is VerificationRequest.Outgoing.CurrentSession -> sessionVerificationService.requestCurrentSessionVerification()
                        is VerificationRequest.Outgoing.User -> sessionVerificationService.requestUserVerification(state.verificationRequest.userId)
                    }
                }
                on { _: Event.DidAcceptVerificationRequest, state ->
                    state.override { State.VerificationRequestAccepted.andLogStateChange() }
                }
            }
            inState<State.StartingSasVerification> {
                onEnterEffect {
                    sessionVerificationService.startVerification()
                }
            }
            inState<State.VerificationRequestAccepted> {
                onActionEffect<Event.StartSasVerification> { _, state ->
                    sessionVerificationService.startVerification()
                }
            }
            inState<State.Canceled> {
                on { _: Event.Reset, state ->
                    state.override { State.Initial.andLogStateChange() }
                }
            }
            inState<State.SasVerificationStarted> {
                on { event: Event.DidReceiveChallenge, state ->
                    state.override { State.Verifying.ChallengeReceived(event.data).andLogStateChange() }
                }
            }
            inState<State.Verifying.ChallengeReceived> {
                on { _: Event.AcceptChallenge, state ->
                    state.override { State.Verifying.Replying(state.snapshot.data, accept = true).andLogStateChange() }
                }
                on { _: Event.DeclineChallenge, state ->
                    state.override { State.Verifying.Replying(state.snapshot.data, accept = false).andLogStateChange() }
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
                    state.override { State.Completed.andLogStateChange() }
                }
            }
            inState {
                logReceivedEvents()
                on { _: Event.DidStartSasVerification, state: MachineState<State> ->
                    state.override { State.SasVerificationStarted.andLogStateChange() }
                }
                on { event: Event.Cancel, state: MachineState<State> ->
                    when (state.snapshot) {
                        State.Initial, State.Completed, is State.Canceled -> state.noChange()
                        State.UseAnotherDevice -> state.override {
                            if (event.returnToRoot) {
                                State.Initial.andLogStateChange()
                            } else {
                                State.Exit.andLogStateChange()
                            }
                        }
                        // For some reason `cancelVerification` is not calling its delegate `didCancel` method so we don't pass from
                        // `Canceling` state to `Canceled` automatically anymore
                        else -> {
                            sessionVerificationService.cancelVerification()
                            state.override { State.Canceled(event.returnToRoot).andLogStateChange() }
                        }
                    }
                }
                on { event: Event.DidCancel, state: MachineState<State> ->
                    state.override { State.Canceled(event.returnToRoot).andLogStateChange() }
                }
                on { event: Event.DidFail, state: MachineState<State> ->
                    when (state.snapshot) {
                        is State.RequestingVerification -> state.override { State.Initial.andLogStateChange() }
                        else -> state.override { State.Canceled(event.returnToRoot).andLogStateChange() }
                    }
                }
            }
        }
    }

    sealed interface State {
        /** The initial state, before verification started. */
        data object Initial : State

        /** Let the user know that they need to get ready on their other session. */
        data object UseAnotherDevice : State

        /** Waiting for verification acceptance. */
        data class RequestingVerification(val verificationRequest: VerificationRequest.Outgoing) : State

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

        /** The verification has been canceled, remotely or locally. */
        data class Canceled(val returnToRoot: Boolean) : State

        /** Verification successful. */
        data object Completed : State

        data object Exit : State
    }

    sealed interface Event {
        /** User wants to use another session. */
        data object UseAnotherDevice : Event

        /** Request verification. */
        data class RequestVerification(val verificationRequest: VerificationRequest.Outgoing) : Event

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
        data class Cancel(val returnToRoot: Boolean) : Event

        /** Verification cancelled. */
        data class DidCancel(val returnToRoot: Boolean) : Event

        /** Request failed. */
        data class DidFail(val returnToRoot: Boolean) : Event

        /** Reset the verification flow to the initial state. */
        data object Reset : Event
    }

    companion object {
        private fun initialState(sessionVerificationService: SessionVerificationService): State {
            return when (sessionVerificationService.sessionVerifiedStatus.value) {
                SessionVerifiedStatus.Unknown, SessionVerifiedStatus.NotVerified -> State.Initial
                SessionVerifiedStatus.Verified -> State.Completed
            }
        }
    }
}

sealed interface VerificationType {
    data object CurrentSession : VerificationType
    data object User : VerificationType
}
