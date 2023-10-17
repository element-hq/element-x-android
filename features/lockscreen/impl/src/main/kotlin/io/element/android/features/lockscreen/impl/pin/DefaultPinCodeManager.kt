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

package io.element.android.features.lockscreen.impl.pin

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.lockscreen.impl.pin.storage.PinCodeStore
import io.element.android.libraries.cryptography.api.CryptoService
import io.element.android.libraries.cryptography.api.EncryptionResult
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

private const val SECRET_KEY_ALIAS = "SECRET_KEY_ALIAS_PIN_CODE"

@ContributesBinding(AppScope::class)
class DefaultPinCodeManager @Inject constructor(
    private val cryptoService: CryptoService,
    private val pinCodeStore: PinCodeStore,
) : PinCodeManager {

    override suspend fun isPinCodeAvailable(): Boolean {
        return pinCodeStore.hasPinCode()
    }

    override suspend fun createPinCode(pinCode: String) {
        val secretKey = cryptoService.getOrCreateSecretKey(SECRET_KEY_ALIAS)
        val encryptedPinCode = cryptoService.encrypt(secretKey, pinCode.toByteArray()).toBase64()
        pinCodeStore.saveEncryptedPinCode(encryptedPinCode)
    }

    override suspend fun verifyPinCode(pinCode: String): Boolean {
        val encryptedPinCode = pinCodeStore.getEncryptedCode() ?: return false
        return try {
            val secretKey = cryptoService.getOrCreateSecretKey(SECRET_KEY_ALIAS)
            val decryptedPinCode = cryptoService.decrypt(secretKey, EncryptionResult.fromBase64(encryptedPinCode))
            decryptedPinCode.contentEquals(pinCode.toByteArray())
        } catch (failure: Throwable) {
            false
        }
    }

    override suspend fun deletePinCode() {
        pinCodeStore.deleteEncryptedPinCode()
    }

    override suspend fun getRemainingPinCodeAttemptsNumber(): Int {
        return pinCodeStore.getRemainingPinCodeAttemptsNumber()
    }

    override suspend fun onWrongPin(): Int {
        return pinCodeStore.onWrongPin()
    }

    override suspend fun resetCounter() {
        pinCodeStore.resetCounter()
    }
}
