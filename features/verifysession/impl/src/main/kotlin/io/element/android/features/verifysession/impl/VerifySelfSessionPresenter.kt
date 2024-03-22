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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.verifysession.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.freeletics.flowredux.compose.rememberStateAndDispatch
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import io.element.android.features.verifysession.impl.VerifySelfSessionStateMachine.Event as StateMachineEvent
import io.element.android.features.verifysession.impl.VerifySelfSessionStateMachine.State as StateMachineState

class VerifySelfSessionPresenter @Inject constructor(
    private val sessionVerificationService: SessionVerificationService,
    private val encryptionService: EncryptionService,
    private val stateMachine: VerifySelfSessionStateMachine,
) : Presenter<VerifySelfSessionState> {
    @Composable
    override fun present(): VerifySelfSessionState {
        LaunchedEffect(Unit) {
            // Force reset, just in case the service was left in a broken state
            sessionVerificationService.reset()
        }
        val recoveryState by encryptionService.recoveryStateStateFlow.collectAsState()
        val stateAndDispatch = stateMachine.rememberStateAndDispatch()
        val verificationFlowStep by remember {
            derivedStateOf {
                stateAndDispatch.state.value.toVerificationStep(
                    canEnterRecoveryKey = recoveryState == RecoveryState.INCOMPLETE
                )
            }
        }
        // Start this after observing state machine
        LaunchedEffect(Unit) {
            observeVerificationService()
        }

        fun handleEvents(event: VerifySelfSessionViewEvents) {
            when (event) {
                VerifySelfSessionViewEvents.RequestVerification -> stateAndDispatch.dispatchAction(StateMachineEvent.RequestVerification)
                VerifySelfSessionViewEvents.StartSasVerification -> stateAndDispatch.dispatchAction(StateMachineEvent.StartSasVerification)
                VerifySelfSessionViewEvents.ConfirmVerification -> stateAndDispatch.dispatchAction(StateMachineEvent.AcceptChallenge)
                VerifySelfSessionViewEvents.DeclineVerification -> stateAndDispatch.dispatchAction(StateMachineEvent.DeclineChallenge)
                VerifySelfSessionViewEvents.CancelAndClose -> stateAndDispatch.dispatchAction(StateMachineEvent.Cancel)
                VerifySelfSessionViewEvents.Reset -> stateAndDispatch.dispatchAction(StateMachineEvent.Reset)
            }
        }
        return VerifySelfSessionState(
            verificationFlowStep = verificationFlowStep,
            eventSink = ::handleEvents,
        )
    }

    private fun StateMachineState?.toVerificationStep(
        canEnterRecoveryKey: Boolean
    ): VerifySelfSessionState.VerificationStep =
        when (val machineState = this) {
            StateMachineState.Initial, null -> {
                VerifySelfSessionState.VerificationStep.Initial(canEnterRecoveryKey = canEnterRecoveryKey)
            }
            StateMachineState.RequestingVerification,
            StateMachineState.StartingSasVerification,
            StateMachineState.SasVerificationStarted,
            StateMachineState.Canceling -> {
                VerifySelfSessionState.VerificationStep.AwaitingOtherDeviceResponse
            }

            StateMachineState.VerificationRequestAccepted -> {
                VerifySelfSessionState.VerificationStep.Ready
            }

            StateMachineState.Canceled -> {
                VerifySelfSessionState.VerificationStep.Canceled
            }

            is StateMachineState.Verifying -> {
                val async = when (machineState) {
                    is StateMachineState.Verifying.Replying -> AsyncData.Loading()
                    else -> AsyncData.Uninitialized
                }
                VerifySelfSessionState.VerificationStep.Verifying(machineState.data, async)
            }

            StateMachineState.Completed -> {
                VerifySelfSessionState.VerificationStep.Completed
            }
        }

    private fun CoroutineScope.observeVerificationService() {
        sessionVerificationService.verificationFlowState.onEach { verificationAttemptState ->
            when (verificationAttemptState) {
                VerificationFlowState.Initial -> stateMachine.dispatch(VerifySelfSessionStateMachine.Event.Reset)
                VerificationFlowState.AcceptedVerificationRequest -> {
                    stateMachine.dispatch(VerifySelfSessionStateMachine.Event.DidAcceptVerificationRequest)
                }
                VerificationFlowState.StartedSasVerification -> {
                    stateMachine.dispatch(VerifySelfSessionStateMachine.Event.DidStartSasVerification)
                }
                is VerificationFlowState.ReceivedVerificationData -> {
                    stateMachine.dispatch(VerifySelfSessionStateMachine.Event.DidReceiveChallenge(verificationAttemptState.data))
                }
                VerificationFlowState.Finished -> {
                    stateMachine.dispatch(VerifySelfSessionStateMachine.Event.DidAcceptChallenge)
                }
                VerificationFlowState.Canceled -> {
                    stateMachine.dispatch(VerifySelfSessionStateMachine.Event.DidCancel)
                }
                VerificationFlowState.Failed -> {
                    stateMachine.dispatch(VerifySelfSessionStateMachine.Event.DidFail)
                }
            }
        }.launchIn(this)
    }
}
