/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.verifysession.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.freeletics.flowredux.compose.rememberStateAndDispatch
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.logout.api.LogoutUseCase
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import io.element.android.features.verifysession.impl.VerifySelfSessionStateMachine.Event as StateMachineEvent
import io.element.android.features.verifysession.impl.VerifySelfSessionStateMachine.State as StateMachineState

class VerifySelfSessionPresenter @AssistedInject constructor(
    @Assisted private val showDeviceVerifiedScreen: Boolean,
    private val sessionVerificationService: SessionVerificationService,
    private val encryptionService: EncryptionService,
    private val stateMachine: VerifySelfSessionStateMachine,
    private val buildMeta: BuildMeta,
    private val sessionPreferencesStore: SessionPreferencesStore,
    private val logoutUseCase: LogoutUseCase,
) : Presenter<VerifySelfSessionState> {
    @AssistedFactory
    interface Factory {
        fun create(showDeviceVerifiedScreen: Boolean): VerifySelfSessionPresenter
    }

    @Composable
    override fun present(): VerifySelfSessionState {
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            // Force reset, just in case the service was left in a broken state
            sessionVerificationService.reset()
        }
        val recoveryState by encryptionService.recoveryStateStateFlow.collectAsState()
        val stateAndDispatch = stateMachine.rememberStateAndDispatch()
        val skipVerification by sessionPreferencesStore.isSessionVerificationSkipped().collectAsState(initial = false)
        val sessionVerifiedStatus by sessionVerificationService.sessionVerifiedStatus.collectAsState()
        val signOutAction = remember {
            mutableStateOf<AsyncAction<String?>>(AsyncAction.Uninitialized)
        }
        val verificationFlowStep by remember {
            derivedStateOf {
                if (skipVerification) {
                    VerifySelfSessionState.VerificationStep.Skipped
                } else {
                    when (sessionVerifiedStatus) {
                        SessionVerifiedStatus.Unknown -> VerifySelfSessionState.VerificationStep.Loading
                        SessionVerifiedStatus.NotVerified -> {
                            stateAndDispatch.state.value.toVerificationStep(
                                canEnterRecoveryKey = recoveryState == RecoveryState.INCOMPLETE
                            )
                        }
                        SessionVerifiedStatus.Verified -> {
                            if (stateAndDispatch.state.value != StateMachineState.Initial || showDeviceVerifiedScreen) {
                                // The user has verified the session, we need to show the success screen
                                VerifySelfSessionState.VerificationStep.Completed
                            } else {
                                // Automatic verification, which can happen on freshly created account, in this case, skip the screen
                                VerifySelfSessionState.VerificationStep.Skipped
                            }
                        }
                    }
                }
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
                VerifySelfSessionViewEvents.Cancel -> stateAndDispatch.dispatchAction(StateMachineEvent.Cancel)
                VerifySelfSessionViewEvents.Reset -> stateAndDispatch.dispatchAction(StateMachineEvent.Reset)
                VerifySelfSessionViewEvents.SignOut -> coroutineScope.signOut(signOutAction)
                VerifySelfSessionViewEvents.SkipVerification -> coroutineScope.launch {
                    sessionPreferencesStore.setSkipSessionVerification(true)
                }
            }
        }
        return VerifySelfSessionState(
            verificationFlowStep = verificationFlowStep,
            signOutAction = signOutAction.value,
            displaySkipButton = buildMeta.isDebuggable,
            eventSink = ::handleEvents,
        )
    }

    private fun StateMachineState?.toVerificationStep(
        canEnterRecoveryKey: Boolean
    ): VerifySelfSessionState.VerificationStep =
        when (val machineState = this) {
            StateMachineState.Initial, null -> {
                VerifySelfSessionState.VerificationStep.Initial(
                    canEnterRecoveryKey = canEnterRecoveryKey,
                    isLastDevice = encryptionService.isLastDevice.value
                )
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

    private fun CoroutineScope.signOut(signOutAction: MutableState<AsyncAction<String?>>) = launch {
        suspend {
            logoutUseCase.logout(ignoreSdkError = true)
        }.runCatchingUpdatingState(signOutAction)
    }
}
