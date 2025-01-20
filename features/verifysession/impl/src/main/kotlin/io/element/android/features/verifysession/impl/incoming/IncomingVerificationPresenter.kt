/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.verifysession.impl.incoming

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.freeletics.flowredux.compose.rememberStateAndDispatch
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.verifysession.impl.incoming.IncomingVerificationState.Step
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.matrix.api.verification.SessionVerificationRequestDetails
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import io.element.android.features.verifysession.impl.incoming.IncomingVerificationStateMachine.Event as StateMachineEvent
import io.element.android.features.verifysession.impl.incoming.IncomingVerificationStateMachine.State as StateMachineState

class IncomingVerificationPresenter @AssistedInject constructor(
    @Assisted private val sessionVerificationRequestDetails: SessionVerificationRequestDetails,
    @Assisted private val navigator: IncomingVerificationNavigator,
    private val sessionVerificationService: SessionVerificationService,
    private val stateMachine: IncomingVerificationStateMachine,
    private val dateFormatter: DateFormatter,
) : Presenter<IncomingVerificationState> {
    @AssistedFactory
    interface Factory {
        fun create(
            sessionVerificationRequestDetails: SessionVerificationRequestDetails,
            navigator: IncomingVerificationNavigator,
        ): IncomingVerificationPresenter
    }

    @Composable
    override fun present(): IncomingVerificationState {
        LaunchedEffect(Unit) {
            // Force reset, just in case the service was left in a broken state
            sessionVerificationService.reset(
                cancelAnyPendingVerificationAttempt = false
            )
            // Acknowledge the request right now
            sessionVerificationService.acknowledgeVerificationRequest(sessionVerificationRequestDetails)
        }
        val stateAndDispatch = stateMachine.rememberStateAndDispatch()
        val formattedSignInTime = remember {
            dateFormatter.format(
                timestamp = sessionVerificationRequestDetails.firstSeenTimestamp,
                mode = DateFormatterMode.TimeOrDate,
            )
        }
        val step by remember {
            derivedStateOf {
                stateAndDispatch.state.value.toVerificationStep(
                    sessionVerificationRequestDetails = sessionVerificationRequestDetails,
                    formattedSignInTime = formattedSignInTime,
                )
            }
        }

        LaunchedEffect(stateAndDispatch.state.value) {
            if ((stateAndDispatch.state.value as? IncomingVerificationStateMachine.State.Initial)?.isCancelled == true) {
                // The verification was canceled before it was started, maybe because another session accepted it
                navigator.onFinish()
            }
        }

        // Start this after observing state machine
        LaunchedEffect(Unit) {
            observeVerificationService()
        }

        fun handleEvents(event: IncomingVerificationViewEvents) {
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
            eventSink = ::handleEvents,
        )
    }

    private fun StateMachineState?.toVerificationStep(
        sessionVerificationRequestDetails: SessionVerificationRequestDetails,
        formattedSignInTime: String,
    ): Step =
        when (val machineState = this) {
            is StateMachineState.Initial,
            IncomingVerificationStateMachine.State.AcceptingIncomingVerification,
            IncomingVerificationStateMachine.State.RejectingIncomingVerification,
            null -> {
                Step.Initial(
                    deviceDisplayName = sessionVerificationRequestDetails.displayName ?: sessionVerificationRequestDetails.deviceId.value,
                    deviceId = sessionVerificationRequestDetails.deviceId,
                    formattedSignInTime = formattedSignInTime,
                    isWaiting = machineState == IncomingVerificationStateMachine.State.AcceptingIncomingVerification ||
                        machineState == IncomingVerificationStateMachine.State.RejectingIncomingVerification,
                )
            }
            is IncomingVerificationStateMachine.State.ChallengeReceived ->
                Step.Verifying(
                    data = machineState.data,
                    isWaiting = false,
                )
            IncomingVerificationStateMachine.State.Completed -> Step.Completed
            IncomingVerificationStateMachine.State.Canceling,
            IncomingVerificationStateMachine.State.Failure -> Step.Failure
            is IncomingVerificationStateMachine.State.AcceptingChallenge ->
                Step.Verifying(
                    data = machineState.data,
                    isWaiting = true,
                )
            is IncomingVerificationStateMachine.State.RejectingChallenge ->
                Step.Verifying(
                    data = machineState.data,
                    isWaiting = true,
                )
            IncomingVerificationStateMachine.State.Canceled -> Step.Canceled
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
                        stateMachine.dispatch(IncomingVerificationStateMachine.Event.DidReceiveChallenge(verificationAttemptState.data))
                    }
                    VerificationFlowState.DidFinish -> {
                        stateMachine.dispatch(IncomingVerificationStateMachine.Event.DidAcceptChallenge)
                    }
                    VerificationFlowState.DidCancel -> {
                        // Can happen when:
                        // - the remote party cancel the verification (before it is started)
                        // - another session has accepted the incoming verification request
                        // - the user reject the challenge from this application (I think this is an error). In this case, the state
                        // machine will ignore this event and change state to Failure.
                        stateMachine.dispatch(IncomingVerificationStateMachine.Event.DidCancel)
                    }
                    VerificationFlowState.DidFail -> {
                        stateMachine.dispatch(IncomingVerificationStateMachine.Event.DidFail)
                    }
                }
            }
            .launchIn(this)
    }
}
