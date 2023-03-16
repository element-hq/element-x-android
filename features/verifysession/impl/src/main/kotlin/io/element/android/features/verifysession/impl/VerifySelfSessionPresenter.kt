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
        val verificationState by remember {
            derivedStateOf { stateMachineStateToViewState(stateMachineCurrentState) }
        }

        fun handleEvents(event: VerifySelfSessionViewEvents) {
            when (event) {
                VerifySelfSessionViewEvents.RequestVerification -> stateMachine.process(SessionVerificationEvent.RequestVerification)
                VerifySelfSessionViewEvents.StartSasVerification -> stateMachine.process(SessionVerificationEvent.StartSasVerification)
                VerifySelfSessionViewEvents.Restart -> stateMachine.process(SessionVerificationEvent.Restart)
                VerifySelfSessionViewEvents.ConfirmVerification -> stateMachine.process(SessionVerificationEvent.AcceptChallenge)
                VerifySelfSessionViewEvents.DeclineVerification -> stateMachine.process(SessionVerificationEvent.DeclineChallenge)
                VerifySelfSessionViewEvents.CancelAndClose -> {
                    if (stateMachineCurrentState !in sequenceOf(
                            SessionVerificationState.Initial,
                            SessionVerificationState.Completed,
                            SessionVerificationState.Canceled
                        )
                    ) {
                        stateMachine.process(SessionVerificationEvent.Cancel)
                    }
                }
            }
        }

        return VerifySelfSessionState(
            verificationState = verificationState,
            eventSink = ::handleEvents,
        )
    }

    private fun stateMachineStateToViewState(state: SessionVerificationState): VerificationState =
        when (state) {
            SessionVerificationState.Initial -> {
                VerificationState.Initial
            }

            SessionVerificationState.RequestingVerification,
            SessionVerificationState.StartingSasVerification,
            SessionVerificationState.SasVerificationStarted,
            SessionVerificationState.VerificationRequestAccepted, SessionVerificationState.Canceling -> {
                VerificationState.AwaitingOtherDeviceResponse
            }

            SessionVerificationState.Canceled -> {
                VerificationState.Canceled
            }

            is SessionVerificationState.Verifying -> {
                val async = when (state) {
                    is SessionVerificationState.Verifying.Replying -> Async.Loading()
                    else -> Async.Uninitialized
                }
                VerificationState.Verifying(state.emojis, async)
            }

            SessionVerificationState.Completed -> {
                VerificationState.Completed
            }
        }
}
