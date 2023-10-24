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

package io.element.android.features.lockscreen.impl.state

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.lockscreen.api.LockScreenState
import io.element.android.features.lockscreen.api.LockScreenStateService
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

//private const val GRACE_PERIOD_IN_MILLIS = 90 * 1000L

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultLockScreenStateService @Inject constructor(
    private val featureFlagService: FeatureFlagService,
    private val pinCodeManager: PinCodeManager,
    private val coroutineScope: CoroutineScope,
) : LockScreenStateService {

    private val _lockScreenState = MutableStateFlow<LockScreenState>(LockScreenState.Unlocked)
    override val state: StateFlow<LockScreenState> = _lockScreenState

    private var lockJob: Job? = null

    init {
        pinCodeManager.addCallback(object : PinCodeManager.Callback {
            override fun onPinCodeVerified() {
                _lockScreenState.value = LockScreenState.Unlocked
            }
        })
        coroutineScope.lockIfNeeded()
    }

    override suspend fun entersForeground() {
        lockJob?.cancel()
    }

    override suspend fun entersBackground() = coroutineScope {
        lockJob = lockIfNeeded()
    }

    private fun CoroutineScope.lockIfNeeded(delayInMillis: Long = 0L) = launch {
        if (featureFlagService.isFeatureEnabled(FeatureFlags.PinUnlock) && pinCodeManager.isPinCodeAvailable()) {
            delay(delayInMillis)
            _lockScreenState.value = LockScreenState.Locked
        }
    }
}
