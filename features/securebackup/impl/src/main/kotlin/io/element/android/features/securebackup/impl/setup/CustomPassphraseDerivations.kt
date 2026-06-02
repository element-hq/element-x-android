/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.setup

import io.element.android.libraries.wellknown.api.CustomRecoveryPassphraseRequirements

/**
 * Validation flags computed once and shared by the presenter and the preview provider.
 *
 * Note: passphrase strength is intentionally NOT derived here. The estimation algorithm is an
 * enterprise capability ([io.element.android.features.enterprise.api.EnterpriseService.estimateCustomRecoveryPassphraseStrength]);
 * the presenter computes it via the injected service and the state provider supplies samples directly.
 */
internal data class CustomPassphraseDerivations(
    val meetsMinLength: Boolean,
    val mismatch: Boolean,
    val canContinueFromEntry: Boolean,
    val canSubmitCustomPassphrase: Boolean,
)

internal fun deriveCustomPassphraseState(
    requirements: CustomRecoveryPassphraseRequirements?,
    passphrase: String,
    confirm: String,
    step: CustomEntryStep,
    setupState: SetupState,
): CustomPassphraseDerivations {
    val meetsMinLength = requirements?.isSatisfiedBy(passphrase) ?: true
    val mismatch = confirm.isNotEmpty() && passphrase != confirm
    val canContinueFromEntry = requirements != null &&
        passphrase.isNotEmpty() &&
        meetsMinLength &&
        setupState is SetupState.Init
    val canSubmit = requirements != null &&
        passphrase.isNotEmpty() &&
        passphrase == confirm &&
        meetsMinLength &&
        step == CustomEntryStep.Confirm &&
        setupState is SetupState.Init
    return CustomPassphraseDerivations(
        meetsMinLength = meetsMinLength,
        mismatch = mismatch,
        canContinueFromEntry = canContinueFromEntry,
        canSubmitCustomPassphrase = canSubmit,
    )
}
