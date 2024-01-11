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
import io.element.android.features.lockscreen.impl.storage.LockScreenStore
import io.element.android.libraries.cryptography.api.EncryptionDecryptionService
import io.element.android.libraries.cryptography.api.EncryptionResult
import io.element.android.libraries.cryptography.api.SecretKeyRepository
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

private const val SECRET_KEY_ALIAS = "elementx.SECRET_KEY_ALIAS_PIN_CODE"

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultPinCodeManager @Inject constructor(
    private val secretKeyRepository: SecretKeyRepository,
    private val encryptionDecryptionService: EncryptionDecryptionService,
    private val lockScreenStore: LockScreenStore,
) : PinCodeManager {
    private val callbacks = CopyOnWriteArrayList<PinCodeManager.Callback>()

    override fun addCallback(callback: PinCodeManager.Callback) {
        callbacks.add(callback)
    }

    override fun removeCallback(callback: PinCodeManager.Callback) {
        callbacks.remove(callback)
    }

    override fun hasPinCode(): Flow<Boolean> {
        return lockScreenStore.hasPinCode()
    }

    override suspend fun getPinCodeSize(): Int {
        val encryptedPinCode = lockScreenStore.getEncryptedCode() ?: return 0
        val secretKey = secretKeyRepository.getOrCreateKey(SECRET_KEY_ALIAS, false)
        val decryptedPinCode = encryptionDecryptionService.decrypt(secretKey, EncryptionResult.fromBase64(encryptedPinCode))
        return decryptedPinCode.size
    }

    override suspend fun createPinCode(pinCode: String) {
        val secretKey = secretKeyRepository.getOrCreateKey(SECRET_KEY_ALIAS, false)
        val encryptedPinCode = encryptionDecryptionService.encrypt(secretKey, pinCode.toByteArray()).toBase64()
        lockScreenStore.saveEncryptedPinCode(encryptedPinCode)
        callbacks.forEach { it.onPinCodeCreated() }
    }

    override suspend fun verifyPinCode(pinCode: String): Boolean {
        val encryptedPinCode = lockScreenStore.getEncryptedCode() ?: return false
        return try {
            val secretKey = secretKeyRepository.getOrCreateKey(SECRET_KEY_ALIAS, false)
            val decryptedPinCode = encryptionDecryptionService.decrypt(secretKey, EncryptionResult.fromBase64(encryptedPinCode))
            val pinCodeToCheck = pinCode.toByteArray()
            decryptedPinCode.contentEquals(pinCodeToCheck).also { isPinCodeCorrect ->
                if (isPinCodeCorrect) {
                    lockScreenStore.resetCounter()
                    callbacks.forEach { callback ->
                        callback.onPinCodeVerified()
                    }
                } else {
                    lockScreenStore.onWrongPin()
                }
            }
        } catch (failure: Throwable) {
            false
        }
    }

    override suspend fun deletePinCode() {
        lockScreenStore.deleteEncryptedPinCode()
        lockScreenStore.resetCounter()
        callbacks.forEach { it.onPinCodeRemoved() }
    }

    override suspend fun getRemainingPinCodeAttemptsNumber(): Int {
        return lockScreenStore.getRemainingPinCodeAttemptsNumber()
    }
}
