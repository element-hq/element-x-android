/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.api

/**
 * Server-driven requirements for a user-chosen recovery passphrase. Today the only rule
 * is a minimum character count; additional rules can be added here as the well-known
 * schema (`custom_recovery_passphrase_settings`) grows.
 */
data class CustomRecoveryPassphraseRequirements(
    val minCharacterCount: Int,
) {
    /** True when [input] meets every active rule. */
    fun isSatisfiedBy(input: String): Boolean = isSatisfiedBy(input.length)

    /** True when an input of [length] characters meets every active rule. */
    fun isSatisfiedBy(length: Int): Boolean = length >= minCharacterCount
}
