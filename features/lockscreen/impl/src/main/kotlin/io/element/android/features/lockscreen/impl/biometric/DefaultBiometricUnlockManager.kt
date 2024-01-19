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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.LockScreenConfig
import io.element.android.features.lockscreen.impl.R
import io.element.android.features.lockscreen.impl.storage.LockScreenStore
import io.element.android.libraries.cryptography.api.EncryptionDecryptionService
import io.element.android.libraries.cryptography.api.SecretKeyRepository
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

private const val SECRET_KEY_ALIAS = "elementx.SECRET_KEY_ALIAS_BIOMETRIC"

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultBiometricUnlockManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lockScreenStore: LockScreenStore,
    private val lockScreenConfig: LockScreenConfig,
    private val encryptionDecryptionService: EncryptionDecryptionService,
    private val secretKeyRepository: SecretKeyRepository,
    private val coroutineScope: CoroutineScope,
) : BiometricUnlockManager {
    private val callbacks = CopyOnWriteArrayList<BiometricUnlock.Callback>()
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
    override fun rememberBiometricUnlock(): BiometricUnlock {
        val isBiometricAllowed by lockScreenStore.isBiometricUnlockAllowed().collectAsState(initial = false)
        val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
        val isAvailable by remember(lifecycleState) {
            derivedStateOf {
                isBiometricAllowed && hasAvailableAuthenticator
            }
        }
        val promptTitle = stringResource(id = R.string.screen_app_lock_biometric_unlock_title_android)
        val promptNegative = stringResource(id = R.string.screen_app_lock_use_pin_android)
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
                DefaultBiometricUnlock(
                    activity = activity,
                    promptInfo = promptInfo,
                    secretKeyRepository = secretKeyRepository,
                    encryptionDecryptionService = encryptionDecryptionService,
                    keyAlias = SECRET_KEY_ALIAS,
                    callbacks = callbacks + internalCallback
                )
            } else {
                NoopBiometricUnlock()
            }
        }
    }

    override fun addCallback(callback: BiometricUnlock.Callback) {
        callbacks.add(callback)
    }

    override fun removeCallback(callback: BiometricUnlock.Callback) {
        callbacks.remove(callback)
    }

    private fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }
}
