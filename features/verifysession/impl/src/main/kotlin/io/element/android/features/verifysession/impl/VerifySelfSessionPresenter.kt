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

package io.element.android.features.verifysession.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import javax.inject.Inject
import io.element.android.features.verifysession.impl.VerifySelfSessionStateMachine.Event as StateMachineEvent
import io.element.android.features.verifysession.impl.VerifySelfSessionStateMachine.State as StateMachineState

class VerifySelfSessionPresenter @Inject constructor(
    private val sessionVerificationService: SessionVerificationService,
) : Presenter<VerifySelfSessionState> {

    @Composable
    override fun present(): VerifySelfSessionState {
        LaunchedEffect(Unit) {
            // Force reset, just in case the service was left in a broken state
            sessionVerificationService.reset()
        }

        val coroutineScope = rememberCoroutineScope()
        val stateMachine = remember { VerifySelfSessionStateMachine(coroutineScope, sessionVerificationService) }

        // Create the new view state from the StateMachine state
        val stateMachineCurrentState by stateMachine.state.collectAsState()
        val verificationFlowState by remember {
            derivedStateOf { stateMachineStateToViewState(stateMachineCurrentState) }
        }

        fun handleEvents(event: VerifySelfSessionViewEvents) {
            when (event) {
                VerifySelfSessionViewEvents.RequestVerification -> stateMachine.process(StateMachineEvent.RequestVerification)
                VerifySelfSessionViewEvents.StartSasVerification -> stateMachine.process(StateMachineEvent.StartSasVerification)
                VerifySelfSessionViewEvents.Restart -> stateMachine.process(StateMachineEvent.Restart)
                VerifySelfSessionViewEvents.ConfirmVerification -> stateMachine.process(StateMachineEvent.AcceptChallenge)
                VerifySelfSessionViewEvents.DeclineVerification -> stateMachine.process(StateMachineEvent.DeclineChallenge)
                VerifySelfSessionViewEvents.CancelAndClose -> {
                    if (stateMachineCurrentState !in sequenceOf(
                            StateMachineState.Initial,
                            StateMachineState.Completed,
                            StateMachineState.Canceled
                        )
                    ) {
                        stateMachine.process(StateMachineEvent.Cancel)
                    }
                }
            }
        }

        return VerifySelfSessionState(
            verificationFlowStep = verificationFlowState,
            eventSink = ::handleEvents,
        )
    }

    private fun stateMachineStateToViewState(state: StateMachineState): VerifySelfSessionState.VerificationStep =
        when (state) {
            StateMachineState.Initial -> {
                VerifySelfSessionState.VerificationStep.Initial
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
                val async = when (state) {
                    is StateMachineState.Verifying.Replying -> Async.Loading()
                    else -> Async.Uninitialized
                }
                VerifySelfSessionState.VerificationStep.Verifying(state.emojis, async)
            }

            StateMachineState.Completed -> {
                VerifySelfSessionState.VerificationStep.Completed
            }
        }
}
