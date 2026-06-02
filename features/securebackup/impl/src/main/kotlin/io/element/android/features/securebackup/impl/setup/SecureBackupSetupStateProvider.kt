/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.setup

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.enterprise.api.CustomRecoveryPassphraseStrength
import io.element.android.features.enterprise.api.CustomRecoveryPassphraseStrengthResult
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.features.securebackup.impl.setup.views.aFormattedRecoveryKey
import io.element.android.libraries.wellknown.api.CustomRecoveryPassphraseRequirements

open class SecureBackupSetupStateProvider : PreviewParameterProvider<SecureBackupSetupState> {
    override val values: Sequence<SecureBackupSetupState>
        get() = sequenceOf(
            aSecureBackupSetupState(wellknownLoaded = false),
            aSecureBackupSetupState(setupState = SetupState.Init),
            aSecureBackupSetupState(setupState = SetupState.Creating),
            aSecureBackupSetupState(setupState = SetupState.Created(aFormattedRecoveryKey())),
            aSecureBackupSetupState(setupState = SetupState.CreatedAndSaved(aFormattedRecoveryKey())),
            aSecureBackupSetupState(
                setupState = SetupState.CreatedAndSaved(aFormattedRecoveryKey()),
                showSaveConfirmationDialog = true,
            ),
            aSecureBackupSetupState(setupState = SetupState.Error(Exception("Test error"))),
            // Custom-entry: empty Entry step
            aSecureBackupSetupState(
                customRecoveryPassphraseRequirements = aPreviewCustomRecoveryPassphraseRequirements(),
                customEntryStep = CustomEntryStep.Entry,
                customPassphrase = "",
            ),
            // Custom-entry: Entry step too-short input
            aSecureBackupSetupState(
                customRecoveryPassphraseRequirements = aPreviewCustomRecoveryPassphraseRequirements(),
                customEntryStep = CustomEntryStep.Entry,
                customPassphrase = "abc",
            ),
            // Custom-entry: Entry step with a Garbage-tier passphrase (zero score, neutral colour)
            aSecureBackupSetupState(
                customRecoveryPassphraseRequirements = aPreviewCustomRecoveryPassphraseRequirements(),
                customEntryStep = CustomEntryStep.Entry,
                customPassphrase = "abcdefgh",
                customPassphraseStrength = CustomRecoveryPassphraseStrengthResult(CustomRecoveryPassphraseStrength.Garbage, score = 0f),
            ),
            // Custom-entry: Entry step with a Weak-tier passphrase
            aSecureBackupSetupState(
                customRecoveryPassphraseRequirements = aPreviewCustomRecoveryPassphraseRequirements(),
                customEntryStep = CustomEntryStep.Entry,
                customPassphrase = "abcde1",
                customPassphraseStrength = CustomRecoveryPassphraseStrengthResult(CustomRecoveryPassphraseStrength.Weak, score = 0.15f),
            ),
            // Custom-entry: Entry step with a Moderate-tier passphrase
            aSecureBackupSetupState(
                customRecoveryPassphraseRequirements = aPreviewCustomRecoveryPassphraseRequirements(),
                customEntryStep = CustomEntryStep.Entry,
                customPassphrase = "Abcdefg1",
                customPassphraseStrength = CustomRecoveryPassphraseStrengthResult(CustomRecoveryPassphraseStrength.Moderate, score = 0.45f),
            ),
            // Custom-entry: Entry step with a Strong-tier passphrase ready to continue
            aSecureBackupSetupState(
                customRecoveryPassphraseRequirements = aPreviewCustomRecoveryPassphraseRequirements(),
                customEntryStep = CustomEntryStep.Entry,
                customPassphrase = "Abcdefg1!@#x",
                customPassphraseStrength = CustomRecoveryPassphraseStrengthResult(CustomRecoveryPassphraseStrength.Strong, score = 0.7f),
            ),
            // Custom-entry: Entry step with a top-tier (Ideal) passphrase
            aSecureBackupSetupState(
                customRecoveryPassphraseRequirements = aPreviewCustomRecoveryPassphraseRequirements(),
                customEntryStep = CustomEntryStep.Entry,
                customPassphrase = "Abcdefg1!@#xyz",
                customPassphraseStrength = CustomRecoveryPassphraseStrengthResult(CustomRecoveryPassphraseStrength.Mega, score = 1f),
            ),
            // Custom-entry: Confirm step empty
            aSecureBackupSetupState(
                customRecoveryPassphraseRequirements = aPreviewCustomRecoveryPassphraseRequirements(),
                customEntryStep = CustomEntryStep.Confirm,
                customPassphrase = "abcdefgh",
                customPassphraseConfirm = "",
            ),
            // Custom-entry: Confirm step mismatch
            aSecureBackupSetupState(
                customRecoveryPassphraseRequirements = aPreviewCustomRecoveryPassphraseRequirements(),
                customEntryStep = CustomEntryStep.Confirm,
                customPassphrase = "abcdefgh",
                customPassphraseConfirm = "different",
            ),
            // Custom-entry: Confirm step ready to submit
            aSecureBackupSetupState(
                customRecoveryPassphraseRequirements = aPreviewCustomRecoveryPassphraseRequirements(),
                customEntryStep = CustomEntryStep.Confirm,
                customPassphrase = "abcdefgh",
                customPassphraseConfirm = "abcdefgh",
            ),
            // Custom-entry: submitting (spinner instead of base58 share UI)
            aSecureBackupSetupState(
                customRecoveryPassphraseRequirements = aPreviewCustomRecoveryPassphraseRequirements(),
                customEntryStep = CustomEntryStep.Confirm,
                setupState = SetupState.Creating,
            ),
            // Custom-entry: SDK error overlay
            aSecureBackupSetupState(
                customRecoveryPassphraseRequirements = aPreviewCustomRecoveryPassphraseRequirements(),
                customEntryStep = CustomEntryStep.Confirm,
                setupState = SetupState.Error(Exception("Test error")),
            ),
        )
}

fun aSecureBackupSetupState(
    isChangeRecoveryKeyUserStory: Boolean = false,
    setupState: SetupState = SetupState.Init,
    showSaveConfirmationDialog: Boolean = false,
    wellknownLoaded: Boolean = true,
    customRecoveryPassphraseRequirements: CustomRecoveryPassphraseRequirements? = null,
    customEntryStep: CustomEntryStep = CustomEntryStep.Entry,
    customPassphrase: String = "",
    customPassphraseConfirm: String = "",
    customPassphraseStrength: CustomRecoveryPassphraseStrengthResult? = null,
): SecureBackupSetupState {
    val derivations = deriveCustomPassphraseState(
        requirements = customRecoveryPassphraseRequirements,
        passphrase = customPassphrase,
        confirm = customPassphraseConfirm,
        step = customEntryStep,
        setupState = setupState,
    )
    return SecureBackupSetupState(
        isChangeRecoveryKeyUserStory = isChangeRecoveryKeyUserStory,
        setupState = setupState,
        showSaveConfirmationDialog = showSaveConfirmationDialog,
        recoveryKeyViewState = setupState.toRecoveryKeyViewState(
            isChangeRecoveryKeyUserStory = isChangeRecoveryKeyUserStory,
            suppressKey = customRecoveryPassphraseRequirements != null,
        ),
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
        eventSink = {}
    )
}

private fun aPreviewCustomRecoveryPassphraseRequirements() = CustomRecoveryPassphraseRequirements(
    minCharacterCount = 8,
)

private fun SetupState.toRecoveryKeyViewState(
    isChangeRecoveryKeyUserStory: Boolean,
    suppressKey: Boolean,
): RecoveryKeyViewState {
    return RecoveryKeyViewState(
        recoveryKeyUserStory = if (isChangeRecoveryKeyUserStory) RecoveryKeyUserStory.Change else RecoveryKeyUserStory.Setup,
        // Match the presenter — the custom-passphrase flow never surfaces the SDK base58 key.
        formattedRecoveryKey = if (suppressKey) null else recoveryKey(),
        displayTextFieldContents = true,
        inProgress = this is SetupState.Creating,
    )
}
