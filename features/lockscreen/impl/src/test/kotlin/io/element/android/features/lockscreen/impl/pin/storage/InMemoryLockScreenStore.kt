/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
