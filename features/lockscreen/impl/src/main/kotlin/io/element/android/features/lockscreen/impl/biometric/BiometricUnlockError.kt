/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.biometric

import androidx.biometric.BiometricPrompt

/**
 * Wrapper for [BiometricPrompt.AuthenticationCallback] errors.
 */
class BiometricUnlockError(val code: Int, message: String) : Exception(message) {
    /**
     * This error disables Biometric authentication, either temporarily or permanently.
     */
    val isAuthDisabledError: Boolean get() = code in LOCKOUT_ERROR_CODES

    /**
     * This error permanently disables Biometric authentication.
     */
    val isAuthPermanentlyDisabledError: Boolean get() = code == BiometricPrompt.ERROR_LOCKOUT_PERMANENT

    companion object {
        private val LOCKOUT_ERROR_CODES = arrayOf(BiometricPrompt.ERROR_LOCKOUT, BiometricPrompt.ERROR_LOCKOUT_PERMANENT)
    }
}
