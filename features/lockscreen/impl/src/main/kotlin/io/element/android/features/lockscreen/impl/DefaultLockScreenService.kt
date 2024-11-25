/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.lockscreen.api.LockScreenLockState
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.features.lockscreen.impl.biometric.BiometricAuthenticatorManager
import io.element.android.features.lockscreen.impl.biometric.DefaultBiometricUnlockCallback
import io.element.android.features.lockscreen.impl.pin.DefaultPinCodeManagerCallback
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import io.element.android.features.lockscreen.impl.storage.LockScreenStore
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultLockScreenService @Inject constructor(
    private val lockScreenConfig: LockScreenConfig,
    private val featureFlagService: FeatureFlagService,
    private val lockScreenStore: LockScreenStore,
    private val pinCodeManager: PinCodeManager,
    private val coroutineScope: CoroutineScope,
    private val sessionObserver: SessionObserver,
    private val appForegroundStateService: AppForegroundStateService,
    biometricAuthenticatorManager: BiometricAuthenticatorManager,
) : LockScreenService {
    private val _lockState = MutableStateFlow<LockScreenLockState>(LockScreenLockState.Unlocked)
    override val lockState: StateFlow<LockScreenLockState> = _lockState

    private var lockJob: Job? = null

    init {
        pinCodeManager.addCallback(object : DefaultPinCodeManagerCallback() {
            override fun onPinCodeVerified() {
                _lockState.value = LockScreenLockState.Unlocked
            }

            override fun onPinCodeRemoved() {
                _lockState.value = LockScreenLockState.Unlocked
            }
        })
        biometricAuthenticatorManager.addCallback(object : DefaultBiometricUnlockCallback() {
            override fun onBiometricAuthenticationSuccess() {
                _lockState.value = LockScreenLockState.Unlocked
                coroutineScope.launch {
                    lockScreenStore.resetCounter()
                }
            }
        })
        coroutineScope.lockIfNeeded()
        observeAppForegroundState()
        observeSessionsState()
    }

    /**
     * Makes sure to delete the pin code when the session is deleted.
     */
    private fun observeSessionsState() {
        sessionObserver.addListener(object : SessionListener {
            override suspend fun onSessionCreated(userId: String) = Unit

            override suspend fun onSessionDeleted(userId: String) {
                // TODO handle multi session at some point
                pinCodeManager.deletePinCode()
            }
        })
    }

    /**
     * Makes sure to lock the app if it goes in background for a certain amount of time.
     */
    private fun observeAppForegroundState() {
        coroutineScope.launch {
            appForegroundStateService.start()
            appForegroundStateService.isInForeground.collect { isInForeground ->
                if (isInForeground) {
                    lockJob?.cancel()
                } else {
                    lockJob = lockIfNeeded(gracePeriod = lockScreenConfig.gracePeriod)
                }
            }
        }
    }

    override fun isPinSetup(): Flow<Boolean> {
        return combine(
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.PinUnlock),
            pinCodeManager.hasPinCode()
        ) { isEnabled, hasPinCode ->
            isEnabled && hasPinCode
        }
    }

    override fun isSetupRequired(): Flow<Boolean> {
        return isPinSetup().map { isPinSetup ->
            !isPinSetup && lockScreenConfig.isPinMandatory
        }
    }

    private fun CoroutineScope.lockIfNeeded(gracePeriod: Duration = Duration.ZERO) = launch {
        if (isPinSetup().first()) {
            delay(gracePeriod)
            _lockState.value = LockScreenLockState.Locked
        }
    }
}
