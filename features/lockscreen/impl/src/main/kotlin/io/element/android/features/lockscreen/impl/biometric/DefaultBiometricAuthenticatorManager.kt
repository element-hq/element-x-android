/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.biometric

import android.app.KeyguardManager
import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.features.lockscreen.impl.LockScreenConfig
import io.element.android.features.lockscreen.impl.R
import io.element.android.features.lockscreen.impl.storage.LockScreenStore
import io.element.android.libraries.cryptography.api.EncryptionDecryptionService
import io.element.android.libraries.cryptography.api.SecretKeyRepository
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

private const val SECRET_KEY_ALIAS = "elementx.SECRET_KEY_ALIAS_BIOMETRIC"

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultBiometricAuthenticatorManager(
    @ApplicationContext private val context: Context,
    private val lockScreenStore: LockScreenStore,
    private val lockScreenConfig: LockScreenConfig,
    private val encryptionDecryptionService: EncryptionDecryptionService,
    private val secretKeyRepository: SecretKeyRepository,
    @AppCoroutineScope
    private val coroutineScope: CoroutineScope,
) : BiometricAuthenticatorManager {
    private val callbacks = CopyOnWriteArrayList<BiometricAuthenticator.Callback>()
    private val biometricManager = BiometricManager.from(context)
    private val keyguardManager: KeyguardManager = context.getSystemService()!!

    /**
     * Returns true if a weak biometric method (i.e.: some face or iris unlock implementations) can be used.
     */
    private val canUseWeakBiometricAuth: Boolean
        get() = lockScreenConfig.isWeakBiometricsEnabled &&
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

    /**
     * Returns true if a strong biometric method (i.e.: fingerprint, some face or iris unlock implementations) can be used.
     */
    private val canUseStrongBiometricAuth: Boolean
        get() = lockScreenConfig.isStrongBiometricsEnabled &&
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    /**
     * Returns true if any biometric method (weak or strong) can be used.
     */
    override val hasAvailableAuthenticator: Boolean
        get() = canUseWeakBiometricAuth || canUseStrongBiometricAuth

    override val isDeviceSecured: Boolean
        get() = keyguardManager.isDeviceSecure

    private val internalCallback = object : DefaultBiometricUnlockCallback() {
        override fun onBiometricSetupError() {
            coroutineScope.launch {
                lockScreenStore.setIsBiometricUnlockAllowed(false)
                secretKeyRepository.deleteKey(SECRET_KEY_ALIAS)
            }
        }
    }

    @Composable
    override fun rememberUnlockBiometricAuthenticator(): BiometricAuthenticator {
        val isBiometricAllowed by remember {
            lockScreenStore.isBiometricUnlockAllowed()
        }.collectAsState(initial = false)
        val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
        val isAvailable by remember(lifecycleState) {
            derivedStateOf { isBiometricAllowed && hasAvailableAuthenticator }
        }
        val promptTitle = stringResource(id = R.string.screen_app_lock_biometric_unlock_title_android)
        val promptNegative = stringResource(id = R.string.screen_app_lock_use_pin_android)
        return rememberBiometricAuthenticator(
            isAvailable = isAvailable,
            promptTitle = promptTitle,
            promptNegative = promptNegative,
        )
    }

    @Composable
    override fun rememberConfirmBiometricAuthenticator(): BiometricAuthenticator {
        val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
        val isAvailable by remember(lifecycleState) {
            derivedStateOf { hasAvailableAuthenticator }
        }
        val promptTitle = stringResource(id = R.string.screen_app_lock_confirm_biometric_authentication_android)
        val promptNegative = stringResource(id = CommonStrings.action_cancel)
        return rememberBiometricAuthenticator(
            isAvailable = isAvailable,
            promptTitle = promptTitle,
            promptNegative = promptNegative,
        )
    }

    @Composable
    private fun rememberBiometricAuthenticator(
        isAvailable: Boolean,
        promptTitle: String,
        promptNegative: String,
    ): BiometricAuthenticator {
        val activity = LocalContext.current.findFragmentActivity()
        return remember(isAvailable) {
            if (isAvailable && activity != null) {
                val authenticators = when {
                    canUseStrongBiometricAuth -> BiometricManager.Authenticators.BIOMETRIC_STRONG
                    canUseWeakBiometricAuth -> BiometricManager.Authenticators.BIOMETRIC_WEAK
                    else -> 0
                }
                val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
                    setTitle(promptTitle)
                    setNegativeButtonText(promptNegative)
                    setAllowedAuthenticators(authenticators)
                }.build()
                DefaultBiometricAuthentication(
                    activity = activity,
                    promptInfo = promptInfo,
                    secretKeyRepository = secretKeyRepository,
                    encryptionDecryptionService = encryptionDecryptionService,
                    keyAlias = SECRET_KEY_ALIAS,
                    callbacks = callbacks + internalCallback
                )
            } else {
                NoopBiometricAuthentication()
            }
        }
    }

    override fun addCallback(callback: BiometricAuthenticator.Callback) {
        callbacks.add(callback)
    }

    override fun removeCallback(callback: BiometricAuthenticator.Callback) {
        callbacks.remove(callback)
    }

    private fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }
}
