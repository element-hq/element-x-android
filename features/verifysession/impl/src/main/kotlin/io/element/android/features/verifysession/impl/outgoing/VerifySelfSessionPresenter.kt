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
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import io.element.android.features.verifysession.impl.outgoing.VerifySelfSessionStateMachine.Event as StateMachineEvent
import io.element.android.features.verifysession.impl.outgoing.VerifySelfSessionStateMachine.State as StateMachineState

class VerifySelfSessionPresenter @AssistedInject constructor(
    @Assisted private val showDeviceVerifiedScreen: Boolean,
    @Assisted private val verificationRequest: VerificationRequest.Outgoing,
    @Assisted private val navigator: Navigator,
    private val sessionVerificationService: SessionVerificationService,
    private val encryptionService: EncryptionService,
    private val buildMeta: BuildMeta,
    private val sessionPreferencesStore: SessionPreferencesStore,
    private val logoutUseCase: LogoutUseCase,
) : Presenter<VerifySelfSessionState> {
    @AssistedFactory
    interface Factory {
        fun create(
            verificationRequest: VerificationRequest.Outgoing,
            showDeviceVerifiedScreen: Boolean,
            navigator: Navigator,
        ): VerifySelfSessionPresenter
    }

    interface Navigator {
        fun pop()
    }

    private val returnToRootWhenCancelled = verificationRequest is VerificationRequest.Outgoing.CurrentSession

    private val stateMachine = VerifySelfSessionStateMachine(
        sessionVerificationService = sessionVerificationService,
        encryptionService = encryptionService,
        verificationType = when (verificationRequest) {
            is VerificationRequest.Outgoing.CurrentSession -> VerificationType.CurrentSession
            is VerificationRequest.Outgoing.User -> VerificationType.User
        }
    )

    @Composable
    override fun present(): VerifySelfSessionState {
        val coroutineScope = rememberCoroutineScope()
        val stateAndDispatch = stateMachine.rememberStateAndDispatch()

        LaunchedEffect(Unit) {
            // Force reset, just in case the service was left in a broken state
            sessionVerificationService.reset(true)
        }
        val recoveryState by encryptionService.recoveryStateStateFlow.collectAsState()
        val sessionVerifiedStatus by sessionVerificationService.sessionVerifiedStatus.collectAsState()
        val signOutAction = remember {
            mutableStateOf<AsyncAction<String?>>(AsyncAction.Uninitialized)
        }
        val step by remember {
            derivedStateOf {
                when (verificationRequest) {
                    is VerificationRequest.Outgoing.CurrentSession -> {
                        when (sessionVerifiedStatus) {
                            SessionVerifiedStatus.Unknown -> VerifySelfSessionState.Step.Loading
                            SessionVerifiedStatus.NotVerified -> {
                                stateAndDispatch.state.value.toVerificationStep(
                                    canEnterRecoveryKey = recoveryState == RecoveryState.INCOMPLETE
                                )
                            }
                            SessionVerifiedStatus.Verified -> {
                                if (stateAndDispatch.state.value != StateMachineState.Initial || showDeviceVerifiedScreen) {
                                    // The user has verified the session, we need to show the success screen
                                    VerifySelfSessionState.Step.Completed
                                } else {
                                    // Automatic verification, which can happen on freshly created account, in this case, skip the screen
                                    VerifySelfSessionState.Step.Skipped
                                }
                            }
                        }
                    }
                    is VerificationRequest.Outgoing.User -> when (stateAndDispatch.state.value) {
                        StateMachineState.Initial -> VerifySelfSessionState.Step.UseAnotherDevice
                        else -> stateAndDispatch.state.value.toVerificationStep(
                            canEnterRecoveryKey = false,
                        )
                    }
                }
            }
        }
        // Start this after observing state machine
        LaunchedEffect(Unit) {
            observeVerificationService()

            if (verificationRequest is VerificationRequest.Outgoing.User) {
                stateMachine.dispatch(StateMachineEvent.UseAnotherDevice)
            }
        }

        val currentStateMachineState = stateAndDispatch.state.value
        LaunchedEffect(currentStateMachineState) {
            if (currentStateMachineState is VerifySelfSessionStateMachine.State.Exit) {
                stateMachine.dispatch(StateMachineEvent.Reset)
                navigator.pop()
            }
        }

        fun handleEvents(event: VerifySelfSessionViewEvents) {
            Timber.d("Verification user action: ${event::class.simpleName}")
            when (event) {
                VerifySelfSessionViewEvents.UseAnotherDevice -> stateAndDispatch.dispatchAction(StateMachineEvent.UseAnotherDevice)
                VerifySelfSessionViewEvents.RequestVerification -> stateAndDispatch.dispatchAction(StateMachineEvent.RequestVerification(verificationRequest))
                VerifySelfSessionViewEvents.StartSasVerification -> stateAndDispatch.dispatchAction(StateMachineEvent.StartSasVerification)
                VerifySelfSessionViewEvents.ConfirmVerification -> stateAndDispatch.dispatchAction(StateMachineEvent.AcceptChallenge)
                VerifySelfSessionViewEvents.DeclineVerification -> stateAndDispatch.dispatchAction(StateMachineEvent.DeclineChallenge)
                VerifySelfSessionViewEvents.Cancel -> stateAndDispatch.dispatchAction(StateMachineEvent.Cancel(returnToRootWhenCancelled))
                VerifySelfSessionViewEvents.Reset -> stateAndDispatch.dispatchAction(StateMachineEvent.Reset)
                VerifySelfSessionViewEvents.SignOut -> coroutineScope.signOut(signOutAction)
                VerifySelfSessionViewEvents.SkipVerification -> coroutineScope.launch {
                    sessionPreferencesStore.setSkipSessionVerification(true)
                }
            }
        }
        return VerifySelfSessionState(
            step = step,
            request = verificationRequest,
            signOutAction = signOutAction.value,
            displaySkipButton = buildMeta.isDebuggable,
            eventSink = ::handleEvents,
        )
    }

    private fun StateMachineState?.toVerificationStep(
        canEnterRecoveryKey: Boolean
    ): VerifySelfSessionState.Step =
        when (val machineState = this) {
            StateMachineState.Initial, null -> {
                VerifySelfSessionState.Step.Initial(
                    canEnterRecoveryKey = canEnterRecoveryKey,
                    isLastDevice = encryptionService.isLastDevice.value
                )
            }
            VerifySelfSessionStateMachine.State.UseAnotherDevice -> {
                VerifySelfSessionState.Step.UseAnotherDevice
            }
            is StateMachineState.RequestingVerification,
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
                VerifySelfSessionState.Step.Skipped
            }
        }

    private fun CoroutineScope.observeVerificationService() {
        sessionVerificationService.verificationFlowState
            .onEach { Timber.d("Verification flow state: ${it::class.simpleName}") }
            .onEach { verificationAttemptState ->
                when (verificationAttemptState) {
                    VerificationFlowState.Initial -> stateMachine.dispatch(VerifySelfSessionStateMachine.Event.Reset)
                    VerificationFlowState.DidAcceptVerificationRequest -> {
                        stateMachine.dispatch(VerifySelfSessionStateMachine.Event.DidAcceptVerificationRequest)
                    }
                    VerificationFlowState.DidStartSasVerification -> {
                        stateMachine.dispatch(VerifySelfSessionStateMachine.Event.DidStartSasVerification)
                    }
                    is VerificationFlowState.DidReceiveVerificationData -> {
                        stateMachine.dispatch(VerifySelfSessionStateMachine.Event.DidReceiveChallenge(verificationAttemptState.data))
                    }
                    VerificationFlowState.DidFinish -> {
                        stateMachine.dispatch(VerifySelfSessionStateMachine.Event.DidAcceptChallenge)
                    }
                    VerificationFlowState.DidCancel -> {
                        stateMachine.dispatch(VerifySelfSessionStateMachine.Event.DidCancel(returnToRootWhenCancelled))
                    }
                    VerificationFlowState.DidFail -> {
                        stateMachine.dispatch(VerifySelfSessionStateMachine.Event.DidFail(returnToRootWhenCancelled))
                    }
                }
            }
            .launchIn(this)
    }

    private fun CoroutineScope.signOut(signOutAction: MutableState<AsyncAction<String?>>) = launch {
        suspend {
            logoutUseCase.logout(ignoreSdkError = true)
        }.runCatchingUpdatingState(signOutAction)
    }
}
