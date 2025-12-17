/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.storage

import kotlinx.coroutines.flow.Flow

interface LockScreenStore : EncryptedPinCodeStorage {
    /**
     * Returns the remaining PIN code attempts. When this reaches 0 the PIN code access won't be available for some time.
     */
    suspend fun getRemainingPinCodeAttemptsNumber(): Int

    /**
     * Should decrement the number of remaining PIN code attempts.
     */
    suspend fun onWrongPin()

    /**
     * Resets the counter of attempts for PIN code and biometric access.
     */
    suspend fun resetCounter()

    /**
     * Returns whether the biometric unlock is allowed or not.
     */
    fun isBiometricUnlockAllowed(): Flow<Boolean>

    /**
     * Sets whether the biometric unlock is allowed or not.
     */
    suspend fun setIsBiometricUnlockAllowed(isAllowed: Boolean)
}
