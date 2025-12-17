/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.biometric

import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.cryptography.api.EncryptionDecryptionService
import io.element.android.libraries.cryptography.api.SecretKeyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber
import java.security.InvalidKeyException
import javax.crypto.Cipher

interface BiometricAuthenticator {
    interface Callback {
        fun onBiometricSetupError()
        fun onBiometricAuthenticationSuccess()
        fun onBiometricAuthenticationFailed(error: Exception?)
    }

    sealed interface AuthenticationResult {
        data object Success : AuthenticationResult
        data class Failure(val error: Exception? = null) : AuthenticationResult
    }

    val isActive: Boolean
    fun setup()
    suspend fun authenticate(): AuthenticationResult
}

class NoopBiometricAuthentication : BiometricAuthenticator {
    override val isActive: Boolean = false
    override fun setup() = Unit
    override suspend fun authenticate() = BiometricAuthenticator.AuthenticationResult.Failure()
}

class DefaultBiometricAuthentication(
    private val activity: FragmentActivity,
    private val promptInfo: PromptInfo,
    private val secretKeyRepository: SecretKeyRepository,
    private val encryptionDecryptionService: EncryptionDecryptionService,
    private val keyAlias: String,
    private val callbacks: List<BiometricAuthenticator.Callback>
) : BiometricAuthenticator {
    override val isActive: Boolean = true

    private var cryptoObject: CryptoObject? = null

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

    override suspend fun authenticate(): BiometricAuthenticator.AuthenticationResult {
        val cryptoObject = cryptoObject ?: return BiometricAuthenticator.AuthenticationResult.Failure()

        val deferredAuthenticationResult = CompletableDeferred<BiometricAuthenticator.AuthenticationResult>()
        val executor = ContextCompat.getMainExecutor(activity.baseContext)
        val callback = AuthenticationCallback(callbacks, deferredAuthenticationResult)
        val prompt = BiometricPrompt(activity, executor, callback)
        prompt.authenticate(promptInfo, cryptoObject)
        return try {
            deferredAuthenticationResult.await()
        } catch (cancellation: CancellationException) {
            prompt.cancelAuthentication()
            BiometricAuthenticator.AuthenticationResult.Failure(cancellation)
        }
    }

    @Throws(KeyPermanentlyInvalidatedException::class)
    private fun ensureKey() = secretKeyRepository.getOrCreateKey(keyAlias, true).also {
        encryptionDecryptionService.createEncryptionCipher(it)
    }
}

private class AuthenticationCallback(
    private val callbacks: List<BiometricAuthenticator.Callback>,
    private val deferredAuthenticationResult: CompletableDeferred<BiometricAuthenticator.AuthenticationResult>,
) : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        val biometricUnlockError = BiometricUnlockError(errorCode, errString.toString())
        callbacks.forEach { it.onBiometricAuthenticationFailed(biometricUnlockError) }
        deferredAuthenticationResult.complete(BiometricAuthenticator.AuthenticationResult.Failure(biometricUnlockError))
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        callbacks.forEach { it.onBiometricAuthenticationFailed(null) }
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        if (result.cryptoObject?.cipher.isValid()) {
            callbacks.forEach { it.onBiometricAuthenticationSuccess() }
            deferredAuthenticationResult.complete(BiometricAuthenticator.AuthenticationResult.Success)
        } else {
            val error = IllegalStateException("Invalid cipher")
            callbacks.forEach { it.onBiometricAuthenticationFailed(error) }
            deferredAuthenticationResult.complete(BiometricAuthenticator.AuthenticationResult.Failure())
        }
    }

    private fun Cipher?.isValid(): Boolean {
        if (this == null) return false
        return runCatchingExceptions {
            doFinal("biometric_challenge".toByteArray())
        }.isSuccess
    }
}
