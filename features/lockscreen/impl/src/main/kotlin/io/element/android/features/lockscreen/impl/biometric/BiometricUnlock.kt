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

package io.element.android.features.lockscreen.impl.biometric

import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import io.element.android.libraries.cryptography.api.EncryptionDecryptionService
import io.element.android.libraries.cryptography.api.SecretKeyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber
import java.security.InvalidKeyException
import javax.crypto.Cipher

interface BiometricUnlock {
    interface Callback {
        fun onBiometricSetupError()
        fun onBiometricUnlockSuccess()
        fun onBiometricUnlockFailed(error: Exception?)
    }

    sealed interface AuthenticationResult {
        data object Success : AuthenticationResult
        data class Failure(val error: Exception? = null) : AuthenticationResult
    }

    val isActive: Boolean
    fun setup()
    suspend fun authenticate(): AuthenticationResult
}

class NoopBiometricUnlock : BiometricUnlock {
    override val isActive: Boolean = false
    override fun setup() = Unit
    override suspend fun authenticate() = BiometricUnlock.AuthenticationResult.Failure()
}

class DefaultBiometricUnlock(
    private val activity: FragmentActivity,
    private val promptInfo: PromptInfo,
    private val secretKeyRepository: SecretKeyRepository,
    private val encryptionDecryptionService: EncryptionDecryptionService,
    private val keyAlias: String,
    private val callbacks: List<BiometricUnlock.Callback>
) : BiometricUnlock {
    override val isActive: Boolean = true

    private lateinit var cryptoObject: CryptoObject

    override fun setup() {
        try {
            val secretKey = ensureKey()
            val cipher = encryptionDecryptionService.createEncryptionCipher(secretKey)
            cryptoObject = CryptoObject(cipher)
        } catch (e: InvalidKeyException) {
            callbacks.forEach { it.onBiometricSetupError() }
            Timber.e(e, "Invalid biometric key")
        }
    }

    override suspend fun authenticate(): BiometricUnlock.AuthenticationResult {
        if (!this::cryptoObject.isInitialized) {
            return BiometricUnlock.AuthenticationResult.Failure()
        }
        val deferredAuthenticationResult = CompletableDeferred<BiometricUnlock.AuthenticationResult>()
        val executor = ContextCompat.getMainExecutor(activity.baseContext)
        val callback = AuthenticationCallback(callbacks, deferredAuthenticationResult)
        val prompt = BiometricPrompt(activity, executor, callback)
        prompt.authenticate(promptInfo, cryptoObject)
        return try {
            deferredAuthenticationResult.await()
        } catch (cancellation: CancellationException) {
            prompt.cancelAuthentication()
            BiometricUnlock.AuthenticationResult.Failure(cancellation)
        }
    }

    @Throws(KeyPermanentlyInvalidatedException::class)
    private fun ensureKey() = secretKeyRepository.getOrCreateKey(keyAlias, true).also {
        encryptionDecryptionService.createEncryptionCipher(it)
    }
}

private class AuthenticationCallback(
    private val callbacks: List<BiometricUnlock.Callback>,
    private val deferredAuthenticationResult: CompletableDeferred<BiometricUnlock.AuthenticationResult>,
) : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        val biometricUnlockError = BiometricUnlockError(errorCode, errString.toString())
        callbacks.forEach { it.onBiometricUnlockFailed(biometricUnlockError) }
        deferredAuthenticationResult.complete(BiometricUnlock.AuthenticationResult.Failure(biometricUnlockError))
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        callbacks.forEach { it.onBiometricUnlockFailed(null) }
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        if (result.cryptoObject?.cipher.isValid()) {
            callbacks.forEach { it.onBiometricUnlockSuccess() }
            deferredAuthenticationResult.complete(BiometricUnlock.AuthenticationResult.Success)
        } else {
            val error = IllegalStateException("Invalid cipher")
            callbacks.forEach { it.onBiometricUnlockFailed(error) }
            deferredAuthenticationResult.complete(BiometricUnlock.AuthenticationResult.Failure())
        }
    }

    private fun Cipher?.isValid(): Boolean {
        if (this == null) return false
        return runCatching {
            doFinal("biometric_challenge".toByteArray())
        }.isSuccess
    }
}
