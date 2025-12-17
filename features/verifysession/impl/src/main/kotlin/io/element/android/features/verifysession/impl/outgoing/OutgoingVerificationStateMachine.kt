/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlin.time.Duration.Companion.seconds
import com.freeletics.flowredux.dsl.State as MachineState

@OptIn(FlowPreview::class)
class OutgoingVerificationStateMachine(
    private val sessionVerificationService: SessionVerificationService,
    private val encryptionService: EncryptionService,
) : FlowReduxStateMachine<OutgoingVerificationStateMachine.State, OutgoingVerificationStateMachine.Event>(
    initialState = State.Initial,
) {
    init {
        spec {
            inState<State.Initial> {
                on<Event.RequestVerification> { event, state ->
                    state.override { State.RequestingVerification(event.verificationRequest).andLogStateChange() }
                }
            }
            inState<State.RequestingVerification> {
                onEnterEffect { event ->
                    when (event.verificationRequest) {
                        is VerificationRequest.Outgoing.CurrentSession -> sessionVerificationService.requestCurrentSessionVerification()
                        is VerificationRequest.Outgoing.User -> sessionVerificationService.requestUserVerification(event.verificationRequest.userId)
                    }
                }
                on<Event.DidAcceptVerificationRequest> { _, state ->
                    state.override { State.VerificationRequestAccepted.andLogStateChange() }
                }
            }
            inState<State.StartingSasVerification> {
                onEnterEffect {
                    sessionVerificationService.startVerification()
                }
            }
            inState<State.VerificationRequestAccepted> {
                on<Event.StartSasVerification> { _, state ->
                    state.override { State.StartingSasVerification.andLogStateChange() }
                }
            }
            inState<State.Canceled> {
                on<Event.Reset> { _, state ->
                    sessionVerificationService.reset(cancelAnyPendingVerificationAttempt = false)
                    state.override { State.Initial.andLogStateChange() }
                }
            }
            inState<State.SasVerificationStarted> {
                on<Event.DidReceiveChallenge> { event, state ->
                    state.override { State.Verifying.ChallengeReceived(event.data).andLogStateChange() }
                }
            }
            inState<State.Verifying.ChallengeReceived> {
                on<Event.AcceptChallenge> { _, state ->
                    state.override { State.Verifying.Replying(state.snapshot.data, accept = true).andLogStateChange() }
                }
                on<Event.DeclineChallenge> { _, state ->
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
                on<Event.DidAcceptChallenge> { _, state ->
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
                on<Event.DidStartSasVerification> { _, state: MachineState<State> ->
                    state.override { State.SasVerificationStarted.andLogStateChange() }
                }
                on<Event.Cancel> { event, state: MachineState<State> ->
                    when (state.snapshot) {
                        State.Initial, State.Completed, is State.Canceled -> state.override { State.Exit }
                        // For some reason `cancelVerification` is not calling its delegate `didCancel` method so we don't pass from
                        // `Canceling` state to `Canceled` automatically anymore
                        else -> {
                            sessionVerificationService.cancelVerification()
                            state.override { State.Canceled.andLogStateChange() }
                        }
                    }
                }
                on<Event.DidCancel> { event, state: MachineState<State> ->
                    state.override { State.Canceled.andLogStateChange() }
                }
                on<Event.DidFail> { event, state: MachineState<State> ->
                    state.override { State.Canceled.andLogStateChange() }
                }
            }
        }
    }

    sealed interface State {
        /** Let the user know that they need to get ready on their other session. */
        data object Initial : State

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
        data object Canceled : State

        /** Verification successful. */
        data object Completed : State

        data object Exit : State
    }

    sealed interface Event {
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
        data object Cancel : Event

        /** Verification cancelled. */
        data object DidCancel : Event

        /** Request failed. */
        data object DidFail : Event

        /** Reset the verification flow to the initial state. */
        data object Reset : Event
    }
}
