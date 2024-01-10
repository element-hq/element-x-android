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

package io.element.android.features.lockscreen.impl.setup.biometric

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.features.lockscreen.impl.storage.LockScreenStore
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetupBiometricPresenter @Inject constructor(
    private val lockScreenStore: LockScreenStore,
) : Presenter<SetupBiometricState> {

    @Composable
    override fun present(): SetupBiometricState {
        var isBiometricSetupDone by remember {
            mutableStateOf(false)
        }

        val coroutineScope = rememberCoroutineScope()

        fun handleEvents(event: SetupBiometricEvents) {
            when (event) {
                SetupBiometricEvents.AllowBiometric -> coroutineScope.launch {
                    lockScreenStore.setIsBiometricUnlockAllowed(true)
                    isBiometricSetupDone = true
                }
                SetupBiometricEvents.UsePin -> coroutineScope.launch {
                    lockScreenStore.setIsBiometricUnlockAllowed(false)
                    isBiometricSetupDone = true
                }
            }
        }

        return SetupBiometricState(
            isBiometricSetupDone = isBiometricSetupDone,
            eventSink = ::handleEvents
        )
    }
}
