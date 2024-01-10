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

package io.element.android.features.lockscreen.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.LockScreenConfig
import io.element.android.features.lockscreen.api.LockScreenLockState
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.features.lockscreen.impl.biometric.BiometricUnlockManager
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
    private val biometricUnlockManager: BiometricUnlockManager,
) : LockScreenService {

    private val _lockScreenState = MutableStateFlow<LockScreenLockState>(LockScreenLockState.Unlocked)
    override val lockState: StateFlow<LockScreenLockState> = _lockScreenState

    private var lockJob: Job? = null

    init {
        pinCodeManager.addCallback(object : DefaultPinCodeManagerCallback() {
            override fun onPinCodeVerified() {
                _lockScreenState.value = LockScreenLockState.Unlocked
            }

            override fun onPinCodeRemoved() {
                _lockScreenState.value = LockScreenLockState.Unlocked
            }
        })
        biometricUnlockManager.addCallback(object : DefaultBiometricUnlockCallback() {
            override fun onBiometricUnlockSuccess() {
                _lockScreenState.value = LockScreenLockState.Unlocked
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
            _lockScreenState.value = LockScreenLockState.Locked
        }
    }
}
