/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.securebackup.impl.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.freeletics.flowredux.compose.StateAndDispatch
import com.freeletics.flowredux.compose.rememberStateAndDispatch
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.securebackup.impl.loggerTagSetup
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.wellknown.api.CustomRecoveryPassphraseRequirements
import io.element.android.libraries.wellknown.api.SessionWellknownRetriever
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

private data class WellknownStatus(
    val loaded: Boolean,
    val customRecoveryPassphraseRequirements: CustomRecoveryPassphraseRequirements?,
)

@AssistedInject
class SecureBackupSetupPresenter(
    @Assisted private val isChangeRecoveryKeyUserStory: Boolean,
    private val stateMachine: SecureBackupSetupStateMachine,
    private val encryptionService: EncryptionService,
    private val sessionWellknownRetriever: SessionWellknownRetriever,
    private val enterpriseService: EnterpriseService,
) : Presenter<SecureBackupSetupState> {
    @AssistedFactory
    interface Factory {
        fun create(isChangeRecoveryKeyUserStory: Boolean): SecureBackupSetupPresenter
    }

    @Composable
    override fun present(): SecureBackupSetupState {
        val coroutineScope = rememberCoroutineScope()
        val stateAndDispatch = stateMachine.rememberStateAndDispatch()
        val setupState by remember {
            derivedStateOf { stateAndDispatch.state.value.toSetupState() }
        }
        var showSaveConfirmationDialog by remember { mutableStateOf(false) }

        // Spinner until the well-known fetch settles, so the user can't start the auto-gen path
        // while a custom spec is still in flight. The loaded/specs pair flips in one assignment.
        val wellknownStatusState = remember {
            mutableStateOf(WellknownStatus(loaded = false, customRecoveryPassphraseRequirements = null))
        }
        val wellknownLoaded by remember {
            derivedStateOf { wellknownStatusState.value.loaded }
        }
        val customRecoveryPassphraseRequirements by remember {
            derivedStateOf { wellknownStatusState.value.customRecoveryPassphraseRequirements }
        }
        // Not rememberSaveable: the passphrase must never reach the on-disk saved-state bundle.
        // Plain String (not a zeroed CharArray): the TextField/event/SDK boundary all copy Strings
        // anyway, so we just clear it on success and accept loss on process death.
        var customPassphrase by remember { mutableStateOf("") }
        var customPassphraseConfirm by remember { mutableStateOf("") }
        var customEntryStep by remember { mutableStateOf(CustomEntryStep.Entry) }
        // Single-flight guard: a button tap and an IME-Done in the same frame both see
        // canSubmit=true and would each launch an enableRecovery coroutine. The state machine
        // dedupes UserCreatesKey, but only this flag stops the duplicate SDK call.
        var customSubmitInFlight by remember { mutableStateOf(false) }
        // Handle to the in-flight custom submit so a back press can abort it (see CancelCustomPassphraseSubmit).
        var customSubmitJob by remember { mutableStateOf<Job?>(null) }

        LaunchedEffect(setupState) {
            if (setupState !is SetupState.Creating) {
                customSubmitInFlight = false
            }
        }

        LaunchedEffect(Unit) {
            val result = sessionWellknownRetriever.getElementWellKnown()
            // Enterprise gate: even if the homeserver advertises a custom spec, only honor it on
            // builds where the feature is enabled. FOSS builds always fall back to the auto-gen path.
            val specsFromWellknown = (result as? WellknownRetrieverResult.Success)?.data?.customRecoveryPassphraseRequirements
            wellknownStatusState.value = WellknownStatus(
                loaded = true,
                customRecoveryPassphraseRequirements = specsFromWellknown.takeIf { enterpriseService.isCustomRecoveryPassphraseEnabled() },
            )
        }

        // Shared with SecureBackupSetupStateProvider so previews never drift from runtime.
        val derivations by remember {
            derivedStateOf {
                deriveCustomPassphraseState(
                    requirements = wellknownStatusState.value.customRecoveryPassphraseRequirements,
                    passphrase = customPassphrase,
                    confirm = customPassphraseConfirm,
                    step = customEntryStep,
                    setupState = setupState,
                )
            }
        }

        // Strength is an enterprise-only estimation; null while the field is empty or in FOSS builds.
        // Only computed on the Entry step, the only place the indicator is rendered.
        val customPassphraseStrength by remember {
            derivedStateOf {
                customPassphrase
                    .takeIf { customEntryStep == CustomEntryStep.Entry && it.isNotEmpty() }
                    ?.let { enterpriseService.estimateCustomRecoveryPassphraseStrength(it) }
            }
        }

        // Nothing to "save" when the user chose the passphrase: auto-skip the Created step.
        LaunchedEffect(setupState, customRecoveryPassphraseRequirements) {
            if (customRecoveryPassphraseRequirements != null && setupState is SetupState.Created) {
                stateAndDispatch.dispatchAction(SecureBackupSetupStateMachine.Event.UserSavedKey)
            }
        }

        fun handleEvent(event: SecureBackupSetupEvents) {
            when (event) {
                SecureBackupSetupEvents.CreateRecoveryKey -> {
                    coroutineScope.createOrChangeRecoveryKey(stateAndDispatch, passphrase = null)
                }
                SecureBackupSetupEvents.RecoveryKeyHasBeenSaved ->
                    stateAndDispatch.dispatchAction(SecureBackupSetupStateMachine.Event.UserSavedKey)
                SecureBackupSetupEvents.DismissDialog -> {
                    showSaveConfirmationDialog = false
                    stateAndDispatch.dispatchAction(SecureBackupSetupStateMachine.Event.ClearError)
                }
                SecureBackupSetupEvents.Done -> {
                    showSaveConfirmationDialog = true
                }
                is SecureBackupSetupEvents.UpdateCustomPassphrase -> {
                    customPassphrase = event.value
                }
                is SecureBackupSetupEvents.UpdateCustomPassphraseConfirm -> {
                    customPassphraseConfirm = event.value
                }
                SecureBackupSetupEvents.ContinueCustomPassphrase -> {
                    if (derivations.canContinueFromEntry) {
                        customEntryStep = CustomEntryStep.Confirm
                    }
                }
                SecureBackupSetupEvents.BackToCustomEntry -> {
                    customEntryStep = CustomEntryStep.Entry
                }
                SecureBackupSetupEvents.SubmitCustomPassphrase -> {
                    if (derivations.canSubmitCustomPassphrase && !customSubmitInFlight) {
                        customSubmitInFlight = true
                        customSubmitJob = coroutineScope.createOrChangeRecoveryKey(
                            stateAndDispatch,
                            passphrase = customPassphrase,
                            onSuccess = {
                                customPassphrase = ""
                                customPassphraseConfirm = ""
                            },
                        )
                    }
                }
                SecureBackupSetupEvents.CancelCustomPassphraseSubmit -> {
                    // Abort the SDK call and snap back to Initial. Dispatch the reset first so that
                    // if the (now cancelled) coroutine still manages to dispatch SdkError/
                    // SdkHasCreatedKey, those land in Initial where the state machine ignores them.
                    customSubmitJob?.cancel()
                    customSubmitJob = null
                    customSubmitInFlight = false
                    stateAndDispatch.dispatchAction(SecureBackupSetupStateMachine.Event.UserCancelledCreate)
                }
            }
        }

        val isCustomFlow = customRecoveryPassphraseRequirements != null
        val recoveryKeyViewState = RecoveryKeyViewState(
            recoveryKeyUserStory = if (isChangeRecoveryKeyUserStory) RecoveryKeyUserStory.Change else RecoveryKeyUserStory.Setup,
            // Custom flow: never surface the SDK base58 key in view state, even during the
            // brief Created/CreatedAndSaved auto-skip window.
            formattedRecoveryKey = if (isCustomFlow) null else setupState.recoveryKey(),
            displayTextFieldContents = true,
            inProgress = setupState is SetupState.Creating,
        )

        return SecureBackupSetupState(
            isChangeRecoveryKeyUserStory = isChangeRecoveryKeyUserStory,
            recoveryKeyViewState = recoveryKeyViewState,
            setupState = setupState,
            showSaveConfirmationDialog = showSaveConfirmationDialog,
            wellknownLoaded = wellknownLoaded,
            customRecoveryPassphraseRequirements = customRecoveryPassphraseRequirements,
            customEntryStep = customEntryStep,
            customPassphrase = customPassphrase,
            customPassphraseConfirm = customPassphraseConfirm,
            customPassphraseMeetsMinLength = derivations.meetsMinLength,
            customPassphraseMismatch = derivations.mismatch,
            customPassphraseStrength = customPassphraseStrength,
            canContinueFromEntry = derivations.canContinueFromEntry,
            canSubmitCustomPassphrase = derivations.canSubmitCustomPassphrase,
            eventSink = ::handleEvent,
        )
    }

    private fun SecureBackupSetupStateMachine.State?.toSetupState(): SetupState {
        return when (this) {
            null,
            SecureBackupSetupStateMachine.State.Initial -> SetupState.Init
            SecureBackupSetupStateMachine.State.CreatingKey -> SetupState.Creating
            is SecureBackupSetupStateMachine.State.KeyCreated -> SetupState.Created(formattedRecoveryKey = key)
            is SecureBackupSetupStateMachine.State.KeyCreatedAndSaved -> SetupState.CreatedAndSaved(formattedRecoveryKey = key)
            is SecureBackupSetupStateMachine.State.Error -> SetupState.Error(exception)
        }
    }

    private fun CoroutineScope.createOrChangeRecoveryKey(
        stateAndDispatch: StateAndDispatch<SecureBackupSetupStateMachine.State, SecureBackupSetupStateMachine.Event>,
        passphrase: String?,
        onSuccess: () -> Unit = {},
    ) = launch {
        stateAndDispatch.dispatchAction(SecureBackupSetupStateMachine.Event.UserCreatesKey)
        // Custom passphrase → enableRecovery(passphrase): the SDK derives the 4S key from it.
        // For the Change flow this rotates in place — the SDK skips backup creation when recovery
        // is already enabled (confirmed), so there's no key-backup teardown or room-key re-upload.
        val result = if (passphrase != null) {
            Timber.tag(loggerTagSetup.value).d("Calling encryptionService.enableRecovery(passphrase=present)")
            encryptionService.enableRecovery(waitForBackupsToUpload = false, passphrase = passphrase)
        } else if (isChangeRecoveryKeyUserStory) {
            Timber.tag(loggerTagSetup.value).d("Calling encryptionService.resetRecoveryKey()")
            encryptionService.resetRecoveryKey()
        } else {
            Timber.tag(loggerTagSetup.value).d("Calling encryptionService.enableRecovery(passphrase=absent)")
            encryptionService.enableRecovery(waitForBackupsToUpload = false, passphrase = null)
        }
        result.fold(
            onSuccess = { key ->
                // Clear buffers only on success; on failure keep them so the user can retry
                // without retyping.
                onSuccess()
                // Custom flow: scrub the SDK base58 key from the state machine so SetupState
                // never carries it. RustEncryptionService likewise keeps it out of
                // enableRecoveryProgressStateFlow for the passphrase path, so it is not retained
                // anywhere once this local goes out of scope.
                val storedKey = if (passphrase != null) "" else key
                stateAndDispatch.dispatchAction(SecureBackupSetupStateMachine.Event.SdkHasCreatedKey(storedKey))
            },
            onFailure = {
                Timber.tag(loggerTagSetup.value).e(it, "Failed to enable recovery")
                // The state machine only accepts Exception; wrap anything else so a failure
                // still leaves the Creating spinner instead of hanging on it.
                val exception = it as? Exception ?: RuntimeException(it)
                stateAndDispatch.dispatchAction(SecureBackupSetupStateMachine.Event.SdkError(exception))
            }
        )
    }
}
