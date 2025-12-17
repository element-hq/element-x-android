/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.verifysession.impl.incoming

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.freeletics.flowredux.compose.rememberStateAndDispatch
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.verifysession.impl.incoming.IncomingVerificationState.Step
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.verification.SessionVerificationRequestDetails
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import io.element.android.features.verifysession.impl.incoming.IncomingVerificationStateMachine.Event as StateMachineEvent
import io.element.android.features.verifysession.impl.incoming.IncomingVerificationStateMachine.State as StateMachineState

@AssistedInject
class IncomingVerificationPresenter(
    @Assisted private val verificationRequest: VerificationRequest.Incoming,
    @Assisted private val navigator: IncomingVerificationNavigator,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
    private val sessionVerificationService: SessionVerificationService,
    private val stateMachine: IncomingVerificationStateMachine,
    private val dateFormatter: DateFormatter,
) : Presenter<IncomingVerificationState> {
    @AssistedFactory
    fun interface Factory {
        fun create(
            verificationRequest: VerificationRequest.Incoming,
            navigator: IncomingVerificationNavigator,
        ): IncomingVerificationPresenter
    }

    @Composable
    override fun present(): IncomingVerificationState {
        val coroutineScope = rememberCoroutineScope()

        val stateAndDispatch = stateMachine.rememberStateAndDispatch()

        DisposableEffect(Unit) {
            coroutineScope.launch {
                // Force reset, just in case the service was left in a broken state
                sessionVerificationService.reset(
                    cancelAnyPendingVerificationAttempt = false
                )

                // Start this after observing state machine
                observeVerificationService()

                // Acknowledge the request right now
                sessionVerificationService.acknowledgeVerificationRequest(verificationRequest)
            }

            onDispose {
                sessionCoroutineScope.launch {
                    val currentState = stateAndDispatch.state.value
                    sessionVerificationService.reset(
                        cancelAnyPendingVerificationAttempt = currentState?.isPending() == true,
                    )
                }
            }
        }

        val formattedSignInTime = remember {
            dateFormatter.format(
                timestamp = verificationRequest.details.firstSeenTimestamp,
                mode = DateFormatterMode.TimeOrDate,
            )
        }
        val step by remember {
            derivedStateOf {
                stateAndDispatch.state.value.toVerificationStep(
                    sessionVerificationRequestDetails = verificationRequest.details,
                    formattedSignInTime = formattedSignInTime,
                )
            }
        }

        LaunchedEffect(stateAndDispatch.state.value) {
            if ((stateAndDispatch.state.value as? StateMachineState.Initial)?.isCancelled == true) {
                // The verification was canceled before it was started, maybe because another session accepted it
                navigator.onFinish()
            }
        }

        fun handleEvent(event: IncomingVerificationViewEvents) {
            Timber.d("Verification user action: ${event::class.simpleName}")
            when (event) {
                IncomingVerificationViewEvents.StartVerification ->
                    stateAndDispatch.dispatchAction(StateMachineEvent.AcceptIncomingRequest)
                IncomingVerificationViewEvents.IgnoreVerification ->
                    navigator.onFinish()
                IncomingVerificationViewEvents.ConfirmVerification ->
                    stateAndDispatch.dispatchAction(StateMachineEvent.AcceptChallenge)
                IncomingVerificationViewEvents.DeclineVerification ->
                    stateAndDispatch.dispatchAction(StateMachineEvent.DeclineChallenge)
                IncomingVerificationViewEvents.GoBack -> {
                    when (val verificationStep = step) {
                        is Step.Initial -> if (verificationStep.isWaiting) {
                            stateAndDispatch.dispatchAction(StateMachineEvent.Cancel)
                        } else {
                            navigator.onFinish()
                        }
                        is Step.Verifying -> if (verificationStep.isWaiting) {
                            // What do we do in this case?
                        } else {
                            stateAndDispatch.dispatchAction(StateMachineEvent.DeclineChallenge)
                        }
                        Step.Canceled,
                        Step.Completed,
                        Step.Failure -> navigator.onFinish()
                    }
                }
            }
        }

        return IncomingVerificationState(
            step = step,
            request = verificationRequest,
            eventSink = ::handleEvent,
        )
    }

    private fun StateMachineState?.toVerificationStep(
        sessionVerificationRequestDetails: SessionVerificationRequestDetails,
        formattedSignInTime: String,
    ): Step =
        when (val machineState = this) {
            is StateMachineState.Initial,
            StateMachineState.AcceptingIncomingVerification,
            StateMachineState.RejectingIncomingVerification,
            null -> {
                Step.Initial(
                    deviceDisplayName = sessionVerificationRequestDetails.deviceDisplayName,
                    deviceId = sessionVerificationRequestDetails.deviceId,
                    formattedSignInTime = formattedSignInTime,
                    isWaiting = machineState == StateMachineState.AcceptingIncomingVerification ||
                        machineState == StateMachineState.RejectingIncomingVerification,
                )
            }
            is StateMachineState.ChallengeReceived ->
                Step.Verifying(
                    data = machineState.data,
                    isWaiting = false,
                )
            StateMachineState.Completed -> Step.Completed
            StateMachineState.Canceling,
            StateMachineState.Failure -> Step.Failure
            is StateMachineState.AcceptingChallenge ->
                Step.Verifying(
                    data = machineState.data,
                    isWaiting = true,
                )
            is StateMachineState.RejectingChallenge ->
                Step.Verifying(
                    data = machineState.data,
                    isWaiting = true,
                )
            StateMachineState.Canceled -> Step.Canceled
        }

    private fun CoroutineScope.observeVerificationService() {
        sessionVerificationService.verificationFlowState
            .onEach { Timber.d("Verification flow state: ${it::class.simpleName}") }
            .onEach { verificationAttemptState ->
                when (verificationAttemptState) {
                    VerificationFlowState.Initial,
                    VerificationFlowState.DidAcceptVerificationRequest,
                    VerificationFlowState.DidStartSasVerification -> Unit
                    is VerificationFlowState.DidReceiveVerificationData -> {
                        stateMachine.dispatch(StateMachineEvent.DidReceiveChallenge(verificationAttemptState.data))
                    }
                    VerificationFlowState.DidFinish -> {
                        stateMachine.dispatch(StateMachineEvent.DidAcceptChallenge)
                    }
                    VerificationFlowState.DidCancel -> {
                        // Can happen when:
                        // - the remote party cancel the verification (before it is started)
                        // - another session has accepted the incoming verification request
                        // - the user reject the challenge from this application (I think this is an error). In this case, the state
                        // machine will ignore this event and change state to Failure.
                        stateMachine.dispatch(StateMachineEvent.DidCancel)
                    }
                    VerificationFlowState.DidFail -> {
                        stateMachine.dispatch(StateMachineEvent.DidFail)
                    }
                }
            }
            .launchIn(this)
    }
}
