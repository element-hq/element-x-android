/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.verifysession.impl.outgoing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.freeletics.flowredux.compose.rememberStateAndDispatch
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import io.element.android.features.verifysession.impl.outgoing.VerifySelfSessionStateMachine.Event as StateMachineEvent
import io.element.android.features.verifysession.impl.outgoing.VerifySelfSessionStateMachine.State as StateMachineState

class VerifySelfSessionPresenter @AssistedInject constructor(
    @Assisted private val showDeviceVerifiedScreen: Boolean,
    @Assisted private val verificationRequest: VerificationRequest.Outgoing,
    private val sessionVerificationService: SessionVerificationService,
    private val encryptionService: EncryptionService,
) : Presenter<VerifySelfSessionState> {
    @AssistedFactory
    interface Factory {
        fun create(
            verificationRequest: VerificationRequest.Outgoing,
            showDeviceVerifiedScreen: Boolean,
        ): VerifySelfSessionPresenter
    }

    private val stateMachine = VerifySelfSessionStateMachine(
        sessionVerificationService = sessionVerificationService,
        encryptionService = encryptionService,
    )

    @Composable
    override fun present(): VerifySelfSessionState {
        val stateAndDispatch = stateMachine.rememberStateAndDispatch()

        val sessionVerifiedStatus by sessionVerificationService.sessionVerifiedStatus.collectAsState()
        val step by remember {
            derivedStateOf {
                when (verificationRequest) {
                    is VerificationRequest.Outgoing.CurrentSession -> {
                        when (sessionVerifiedStatus) {
                            SessionVerifiedStatus.Unknown -> VerifySelfSessionState.Step.Loading
                            SessionVerifiedStatus.NotVerified -> {
                                stateAndDispatch.state.value.toVerificationStep()
                            }
                            SessionVerifiedStatus.Verified -> {
                                if (stateAndDispatch.state.value != StateMachineState.Initial || showDeviceVerifiedScreen) {
                                    // The user has verified the session, we need to show the success screen
                                    VerifySelfSessionState.Step.Completed
                                } else {
                                    // Automatic verification, which can happen on freshly created account, in this case, skip the screen
                                    VerifySelfSessionState.Step.Exit
                                }
                            }
                        }
                    }
                    is VerificationRequest.Outgoing.User -> stateAndDispatch.state.value.toVerificationStep()
                }
            }
        }

        // Start this after observing state machine
        LaunchedEffect(Unit) {
            // Force reset, just in case the service was left in a broken state
            sessionVerificationService.reset(cancelAnyPendingVerificationAttempt = true)

            observeVerificationService()
        }

        fun handleEvents(event: VerifySelfSessionViewEvents) {
            Timber.d("Verification user action: ${event::class.simpleName}")
            when (event) {
                // Just relay the event to the state machine
                VerifySelfSessionViewEvents.RequestVerification -> stateAndDispatch.dispatchAction(StateMachineEvent.RequestVerification(verificationRequest))
                VerifySelfSessionViewEvents.StartSasVerification -> stateAndDispatch.dispatchAction(StateMachineEvent.StartSasVerification)
                VerifySelfSessionViewEvents.ConfirmVerification -> stateAndDispatch.dispatchAction(StateMachineEvent.AcceptChallenge)
                VerifySelfSessionViewEvents.DeclineVerification -> stateAndDispatch.dispatchAction(StateMachineEvent.DeclineChallenge)
                VerifySelfSessionViewEvents.Cancel -> stateAndDispatch.dispatchAction(StateMachineEvent.Cancel)
                VerifySelfSessionViewEvents.Reset -> stateAndDispatch.dispatchAction(StateMachineEvent.Reset)
            }
        }
        return VerifySelfSessionState(
            step = step,
            request = verificationRequest,
            eventSink = ::handleEvents,
        )
    }

    private fun StateMachineState?.toVerificationStep(): VerifySelfSessionState.Step =
        when (val machineState = this) {
            StateMachineState.Initial, null -> {
                VerifySelfSessionState.Step.Initial
            }
            is StateMachineState.RequestingVerification,
            is StateMachineState.StartingSasVerification,
            StateMachineState.SasVerificationStarted -> {
                VerifySelfSessionState.Step.AwaitingOtherDeviceResponse
            }

            StateMachineState.VerificationRequestAccepted -> {
                VerifySelfSessionState.Step.Ready
            }

            is StateMachineState.Canceled -> {
                VerifySelfSessionState.Step.Canceled
            }

            is StateMachineState.Verifying -> {
                val async = when (machineState) {
                    is StateMachineState.Verifying.Replying -> AsyncData.Loading()
                    else -> AsyncData.Uninitialized
                }
                VerifySelfSessionState.Step.Verifying(machineState.data, async)
            }

            StateMachineState.Completed -> {
                VerifySelfSessionState.Step.Completed
            }

            StateMachineState.Exit -> {
                VerifySelfSessionState.Step.Exit
            }
        }

    private fun CoroutineScope.observeVerificationService() {
        sessionVerificationService.verificationFlowState
            .onEach { Timber.d("Verification flow state: ${it::class.simpleName}") }
            .onEach { verificationAttemptState ->
                when (verificationAttemptState) {
                    VerificationFlowState.Initial -> stateMachine.dispatch(StateMachineEvent.Reset)
                    VerificationFlowState.DidAcceptVerificationRequest -> {
                        stateMachine.dispatch(StateMachineEvent.DidAcceptVerificationRequest)
                    }
                    VerificationFlowState.DidStartSasVerification -> {
                        stateMachine.dispatch(StateMachineEvent.DidStartSasVerification)
                    }
                    is VerificationFlowState.DidReceiveVerificationData -> {
                        stateMachine.dispatch(StateMachineEvent.DidReceiveChallenge(verificationAttemptState.data))
                    }
                    VerificationFlowState.DidFinish -> {
                        stateMachine.dispatch(StateMachineEvent.DidAcceptChallenge)
                    }
                    VerificationFlowState.DidCancel -> {
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
