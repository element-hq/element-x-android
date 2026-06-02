/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.setup

import io.element.android.features.enterprise.api.CustomRecoveryPassphraseStrengthResult
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.libraries.wellknown.api.CustomRecoveryPassphraseRequirements

data class SecureBackupSetupState(
    val isChangeRecoveryKeyUserStory: Boolean,
    val recoveryKeyViewState: RecoveryKeyViewState,
    val showSaveConfirmationDialog: Boolean,
    val setupState: SetupState,
    /** False while the well-known is still being fetched; the view shows a spinner until true. */
    val wellknownLoaded: Boolean,
    /** Non-null when the well-known requires a user-chosen passphrase; hides the generated-key UI. */
    val customRecoveryPassphraseRequirements: CustomRecoveryPassphraseRequirements?,
    val customEntryStep: CustomEntryStep,
    /** User-typed passphrase. Not persisted to saved-state; cleared after a successful submit. */
    val customPassphrase: String,
    /** Confirmation field; same lifetime contract as [customPassphrase]. */
    val customPassphraseConfirm: String,
    /** True when [customPassphrase] satisfies the minimum-character-count rule from [customRecoveryPassphraseRequirements]. */
    val customPassphraseMeetsMinLength: Boolean,
    val customPassphraseMismatch: Boolean,
    /** Null while the Entry-step passphrase is empty; otherwise the latest strength reading rendered below the field. */
    val customPassphraseStrength: CustomRecoveryPassphraseStrengthResult?,
    /** True when the Entry-step passphrase is valid enough to advance to the Confirm step. */
    val canContinueFromEntry: Boolean,
    val canSubmitCustomPassphrase: Boolean,
    val eventSink: (SecureBackupSetupEvents) -> Unit
)

enum class CustomEntryStep {
    Entry,
    Confirm,
}

sealed interface SetupState {
    data object Init : SetupState
    data object Creating : SetupState
    data class Created(val formattedRecoveryKey: String) : SetupState
    data class CreatedAndSaved(val formattedRecoveryKey: String) : SetupState
    data class Error(val exception: Exception) : SetupState
}

fun SetupState.recoveryKey(): String? = when (this) {
    is SetupState.Created -> formattedRecoveryKey
    is SetupState.CreatedAndSaved -> formattedRecoveryKey
    else -> null
}
