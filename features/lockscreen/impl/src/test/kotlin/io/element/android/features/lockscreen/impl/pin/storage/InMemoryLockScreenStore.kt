/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.pin.storage

import io.element.android.features.lockscreen.impl.storage.LockScreenStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private const val DEFAULT_REMAINING_ATTEMPTS = 3

class InMemoryLockScreenStore : LockScreenStore {
    private val hasPinCode = MutableStateFlow(false)
    private var pinCode: String? = null
        set(value) {
            field = value
            hasPinCode.value = value != null
        }
    private var remainingAttempts: Int = DEFAULT_REMAINING_ATTEMPTS
    private var isBiometricUnlockAllowed = MutableStateFlow(false)

    override suspend fun getRemainingPinCodeAttemptsNumber(): Int {
        return remainingAttempts
    }

    override suspend fun onWrongPin() {
        remainingAttempts--
    }

    override suspend fun resetCounter() {
        remainingAttempts = DEFAULT_REMAINING_ATTEMPTS
    }

    override suspend fun getEncryptedCode(): String? {
        return pinCode
    }

    override suspend fun saveEncryptedPinCode(pinCode: String) {
        this.pinCode = pinCode
    }

    override suspend fun deleteEncryptedPinCode() {
        pinCode = null
    }

    override fun hasPinCode(): Flow<Boolean> {
        return hasPinCode
    }

    override fun isBiometricUnlockAllowed(): Flow<Boolean> {
        return isBiometricUnlockAllowed
    }

    override suspend fun setIsBiometricUnlockAllowed(isAllowed: Boolean) {
        isBiometricUnlockAllowed.value = isAllowed
    }
}
