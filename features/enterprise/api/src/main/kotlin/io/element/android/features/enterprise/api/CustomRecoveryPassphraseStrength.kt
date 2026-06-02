/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api

/**
 * Strength tier surfaced to the user under a custom recovery passphrase field.
 * The eight tiers mirror iOS `PasswordStrengthThreshold` 1:1 so both clients label a given
 * passphrase identically; each maps to its own label and time-to-crack description in the UI.
 */
enum class CustomRecoveryPassphraseStrength {
    Garbage,
    Weak,
    Moderate,
    Okay,
    Strong,
    VeryStrong,
    UltraStrong,
    Mega,
}

/**
 * Result of estimating a custom recovery passphrase's strength. The estimation algorithm itself
 * is an enterprise capability ([EnterpriseService.estimateCustomRecoveryPassphraseStrength]); FOSS
 * builds never produce one.
 */
data class CustomRecoveryPassphraseStrengthResult(
    val strength: CustomRecoveryPassphraseStrength,
    /** Fill amount for the progress bar. Always in `0f..1f`. */
    val score: Float,
)
